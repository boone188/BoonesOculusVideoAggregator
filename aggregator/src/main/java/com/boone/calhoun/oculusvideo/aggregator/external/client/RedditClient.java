package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.boone.calhoun.oculusvideo.aggregator.data.RedditVideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoPlayer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RedditClient 
{
	public enum ListType {HOT, TOP};
	public enum TimeRange {HOUR, DAY, WEEK};
	
	private static Logger logger = LogManager.getLogger(RedditClient.class);

	private static String REDDIT_LINK_KIND = "t3";
	private static String REDDIT_COMMENT_KIND = "t1";
	
	private Client client;
	
	public RedditClient()
	{
		client = ClientBuilder.newClient();
	}
	
	public List<RedditVideoInfo> getVideoList(VideoPlayer playerType, ListType listType, TimeRange timeRange, int count)
	{
		List<RedditVideoInfo> videoInfoList = new ArrayList<RedditVideoInfo>();
		
		// Make the call to Reddit
		WebTarget target = client.target("http://reddit.com/r/oculus")
	            .path("search.json")
	            .queryParam("q", "site:" + playerType.name().toLowerCase())	// Search for links from the video player site
	            .queryParam("t", timeRange.name().toLowerCase()) 				// Time range covered
	            .queryParam("sort", listType.name().toLowerCase()) 			// Sorting type
	            .queryParam("restrict_sr", "on") 								// Restrict to the oculus subreddit
	            .queryParam("limit", count);									// Limit the number of results
		
		logger.debug("Making query to Reddit: " + target.getUriBuilder());
		
	    String strResponse = target.request(MediaType.APPLICATION_JSON).get(String.class);
		
		logger.debug("Response from Reddit: " + strResponse);
		
		// deserialize the JSON response
		RedditListingsResponseData responseData = null;
		try
		{
			Gson gson = new Gson();
			RedditListingsResponse response = gson.fromJson(strResponse, RedditListingsResponse.class);
			responseData = response.data;
		}
		catch (JsonSyntaxException e)
		{
			logger.error("Error deserializing response from Reddit", e);
			throw e;
		}
		
		// inspect all the listings for videos
		for (RedditListing listing : responseData.children)
		{
			logger.debug("Inspecting reddit listing: " + listing);
			if (REDDIT_LINK_KIND.equals(listing.kind) && listing.data != null)
			{
				RedditListingData listingData = listing.data;
				
				// if there is no media, it certainly isn't a video
				if (listingData.media != null && listingData.media.oembed != null)
				{
					logger.debug("The listing is a media type");
					RedditListingMediaOembed oembed = listingData.media.oembed;
					
					// we are only interested in video types
					if ("video".equals(oembed.type))
					{
						logger.debug("The listing is a video");
						
						// check if the video provider is one we know and care about
						RedditVideoProvider redditVideoProvider = RedditVideoProvider.fromRedditProviderName(oembed.provider_name);
						if (redditVideoProvider != null)
						{
							logger.debug("The video is from a known provider");
						
							try
							{
								// build the video info
								RedditVideoInfo videoInfo = new RedditVideoInfo();
								VideoInfo.Identifier identifier = 
										new VideoInfo.Identifier(
												redditVideoProvider.getPlayerType(), 
												redditVideoProvider.parseId(oembed.url));
								videoInfo.setIdentifier(identifier);
								videoInfo.setTitle(listingData.title);
								videoInfo.setUrl("http://reddit.com" + listingData.permalink);
								videoInfo.setScore(listingData.score);
								videoInfo.setUpvotes(listingData.ups);
								videoInfo.setDownvotes(listingData.downs);
								videoInfo.setOver18(listingData.over_18);
								videoInfo.setHot(ListType.HOT.equals(listType));
								
								// add it to the list
								videoInfoList.add(videoInfo);
								
								logger.debug("Adding video info: " + videoInfo);
							}
							catch (Exception e)
							{
								// log the exception and continue
								logger.error("Error gathering info for video", e);
							}
						}
					}
				}
			}
		}
		
		logger.debug("Found " + videoInfoList.size() + " videos");
		
		// Find the best comments
		for (RedditVideoInfo redditVideoInfo : videoInfoList)
		{
			try
			{
				// Make the call to Reddit
				String commentsTarget = redditVideoInfo.getUrl();
				if (commentsTarget.endsWith("/"))
				{
					commentsTarget = commentsTarget.substring(0, commentsTarget.length()-1) + ".json";
				}
				else
				{
					commentsTarget += ".json";
				}
				
				String strCommentResponse = client.target(commentsTarget)
			            .request(MediaType.APPLICATION_JSON)
			            .get(String.class);
				
				// Deserialize the JSON response
				Gson gson = new Gson();
				RedditListingsResponse[] commentResponses = gson.fromJson(strCommentResponse, RedditListingsResponse[].class);
				
				int topCommentScore = 0;
				String topCommentAuthor = null;
				String topCommentText = null;
				
				for (RedditListingsResponse commentResponse : commentResponses)
				{
					RedditListingsResponseData commentResponseData = commentResponse.data;
				
					for (RedditListing listing : commentResponseData.children)
					{
						// Only interested in the comments listings
						if (REDDIT_COMMENT_KIND.equals(listing.kind) && listing.data != null)
						{
							RedditListingData listingData = listing.data;
							
							int score = listingData.ups - listingData.downs;
							if (score > topCommentScore)
							{
								topCommentAuthor = listingData.author;
								topCommentText = listingData.body;
								topCommentScore = score;
							}
						}
					}
				}
				
				redditVideoInfo.setTopCommentAuthor(topCommentAuthor);
				redditVideoInfo.setTopCommentText(topCommentText);
				redditVideoInfo.setTopCommentScore(topCommentScore);
			}
			catch (Exception e)
			{
				logger.error("Error getting comments for video on Reddit", e);
			}
		}
		
		return videoInfoList;
	}
	
}

class RedditListingsResponse
{
	public RedditListingsResponseData data = null;
}

class RedditListingsResponseData
{
	RedditListing[] children = null;
}

class RedditListing
{
	String kind = null;
	RedditListingData data = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("kind", kind)
			.append("data", data)
			.toString();
	}
}

class RedditListingData
{
	String permalink = null;
	String author = null;
	String body = null;
	int score = 0;
	int ups = 0;
	int downs = 0;
	String title = null;
	boolean over_18 = false;
	
	RedditListingMedia media = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("permalink", permalink)
			.append("author", author)
			.append("body", body)
			.append("score", score)
			.append("ups", ups)
			.append("downs", downs)
			.append("title", title)
			.append("over_18", over_18)
			.append("media", media)
			.toString();
	}
}

class RedditListingMedia
{
	RedditListingMediaOembed oembed = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("oembed", oembed)
			.toString();
	}
}

class RedditListingMediaOembed
{
	String type = null;
	String title = null;
	String url = null;
	String provider_name = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("type", type)
			.append("title", title)
			.append("url", url)
			.append("provider_name", provider_name)
			.toString();
	}
}

enum RedditVideoProvider
{
	YOUTUBE("YouTube", VideoPlayer.YOUTUBE,
			"(?:http|https|)(?::\\/\\/|)(?:www.|)(?:youtu\\.be\\/|youtube\\.com(?:\\/embed\\/|\\/v\\/|\\/watch\\?v=|\\/ytscreeningroom\\?v=|\\/feeds\\/api\\/videos\\/|\\/user\\S*[^\\w\\-\\s]|\\S*[^\\w\\-\\s]))([\\w\\-]{11})[a-z0-9;:@?&%=+\\/\\$_.-]*");
	
	private String redditProviderName;
	private VideoPlayer playerType;
	private Pattern idMatchPattern;
	
	RedditVideoProvider(String redditProviderName, VideoPlayer playerType, String idMatchPattern)
	{
		this.redditProviderName = redditProviderName;
		this.playerType = playerType;
		this.idMatchPattern = Pattern.compile(idMatchPattern);
	}
	
	public VideoPlayer getPlayerType()
	{
		return this.playerType;
	}
	
	public String parseId(String input)
	{
		Matcher idMatcher = idMatchPattern.matcher(input);
		if (idMatcher.matches())
		{
			return idMatcher.group(1);
		}
		
		throw new IllegalArgumentException("Cannot find ID in input string: " + input);
	}
	
	static RedditVideoProvider fromRedditProviderName(String redditProviderName)
	{
		RedditVideoProvider result = null;
		
		for (RedditVideoProvider provider : RedditVideoProvider.values())
		{
			if (provider.redditProviderName.equals(redditProviderName))
			{
				return provider;
			}
		}
		
		return result;
	}
}