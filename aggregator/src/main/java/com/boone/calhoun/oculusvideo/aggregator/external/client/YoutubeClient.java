package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.boone.calhoun.oculusvideo.aggregator.data.VideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoPlayer;
import com.boone.calhoun.oculusvideo.aggregator.data.YoutubeVideoInfo;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeQuery.OrderBy;
import com.google.gdata.client.youtube.YouTubeQuery.Time;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Person;
import com.google.gdata.data.extensions.Comments;
import com.google.gdata.data.extensions.FeedLink;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.CommentEntry;
import com.google.gdata.data.youtube.CommentFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

public class YoutubeClient 
{
	private static Logger logger = LogManager.getLogger(YoutubeClient.class);
	
	private static String APP_NAME = "BoonesOculusVideoAggregator";
	private static String DEVELOPER_KEY = "AI39si7r4D-Ozg7zsCAfV9oNVYxS_6Ob7tTmvjVGMMKhTN-jfVjTyFoiGvRnHcJ-Hka1xShh6LTnv0a2sRo-ZtpygZsRnHhkMA";
	private static String VIDEO_ENTRY_URL_BASE = "http://gdata.youtube.com/feeds/api/videos/";
	private static String ID_PATTERN = "\\S+?video:([\\w\\-]{11})";
	
	private YouTubeService service = null;
	private Pattern idPattern = null;
	
	public YoutubeClient()
	{
		service = new YouTubeService(APP_NAME, DEVELOPER_KEY);
		idPattern = Pattern.compile(ID_PATTERN);
	}
	
	/**
	 * Get a list of videos based on query parameters.
	 * 
	 * @param queryText	The text to search for
	 * @param maxCount	The maximum number of videos to return
	 * @param orderBy	How to order the results
	 * @param time		The time range of videos to consider
	 * @return List<VideoInfo>
	 * @throws Exception if unable to query for videos
	 */
	public List<VideoInfo> getVideoList(String queryText, int maxCount, OrderBy orderBy, Time time) throws Exception
	{
		List<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();
		
		YouTubeQuery query = null;
		try 
		{
			query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
			query.setFullTextQuery(queryText);
			query.setMaxResults(maxCount);
			if(OrderBy.RELEVANCE.equals(orderBy))
			{
				query.setOrderByRelevanceForLanguage("en");
			}
			else
			{
				query.setOrderBy(orderBy);
			}
			query.setTime(time);
			logger.info("Performing Youtube query: " + query.getUrl().toString());
		} 
		catch (MalformedURLException e) 
		{
			logger.error("Error creating YouTube query", e);
			throw new Exception("Error creating YouTube query");
		}
		
		VideoFeed videoFeed = null;
		try 
		{
			videoFeed = service.query(query, VideoFeed.class);
		}
		catch (Exception e)
		{
			logger.error("Error querying YouTube for videos", e);
			throw new Exception("Error querying YoutTube for videos");
		}
		
		List<VideoEntry> videoEntries = videoFeed.getEntries();
		for (VideoEntry videoEntry : videoEntries)
		{
			logger.debug("Inspecting video entry: " + ToStringBuilder.reflectionToString(videoEntry));
			VideoInfo videoInfo = convertVideoEntry(videoEntry);
			if (videoInfo != null)
			{
				// The search API returns really inaccurate statistics, so the info for each video needs
				// to be retrieved individually for better quality.
				VideoInfo detailedVideoInfo = null;
				try
				{
					detailedVideoInfo = getVideoInfo(videoInfo.getIdentifier().getId());
				}
				catch (Exception e)
				{
					logger.warn("Unable to get detailed info for video. Falling back on info from search.", e);
				}
				
				if (detailedVideoInfo != null)
				{
					videoInfoList.add(detailedVideoInfo);
				}
				else
				{
					videoInfoList.add(videoInfo);
				}
			}
		}
		
		return videoInfoList;
	}
	
	/**
	 * Get video info by Youtube ID.
	 * 
	 * @param id	The Youtube video ID
	 * @return VideoInfo or null if the video entry cannot be converted to a VideoInfo
	 * @throws Exception if unable to query for the video
	 */
	public VideoInfo getVideoInfo(String id) throws Exception
	{
		VideoInfo videoInfo = null;
		
		String videoEntryUrl = VIDEO_ENTRY_URL_BASE + id;
		try 
		{
			logger.debug("Performing Youtube query: " + videoEntryUrl);
			VideoEntry videoEntry = service.getEntry(new URL(videoEntryUrl), VideoEntry.class);
			videoInfo = convertVideoEntry(videoEntry);
			
			// Add comment info if available
			addCommentInfo(videoInfo);
		}
		catch (Exception e)
		{
			logger.error("Error getting video with id " + id, e);
			throw e;
		}
		
		return videoInfo;
	}
	
	/**
	 * Get top comment info from Youtube and add it to the video info.
	 * 
	 * @param videoInfo The base VideoInfo to add comment info to.
	 */
	private void addCommentInfo(VideoInfo videoInfo)
	{
		if (videoInfo.getYoutubeVideoInfo() != null && videoInfo.getYoutubeVideoInfo().getCommentFeedUrl() != null)
		{
			try
			{
				logger.debug("Getting Youtube comment feed: " + videoInfo.getYoutubeVideoInfo().getCommentFeedUrl());
				URL feedUrl = new URL(videoInfo.getYoutubeVideoInfo().getCommentFeedUrl());
				CommentFeed commentFeed = service.getFeed(feedUrl, CommentFeed.class);
				
				if (commentFeed != null)
				{
					// Have to rely on Youtube ordering the comments from top to bottom by their rating,
					// because they don't include the rating for each comment.
					for (CommentEntry comment : commentFeed.getEntries())
					{
						for (Person author : comment.getAuthors())
						{
							String authorName = StringUtils.trim(StringUtils.strip(author.getName()));
							
							// Have to remove this BOM that is omnipresent & newlines
							String commentText = comment.getPlainTextContent().replace("\ufeff", "").replace("\n", "");
							commentText = StringUtils.strip(StringUtils.trim(commentText));
							
							// The first comment with an author with a real name should be the top comment
							if ( ! "YouTube".equalsIgnoreCase(author.getName()) 
									&& ! StringUtils.isBlank(authorName)
									&& ! StringUtils.isBlank(commentText))
							{
								videoInfo.getYoutubeVideoInfo().setTopCommentAuthor(authorName);
								videoInfo.getYoutubeVideoInfo().setTopCommentText(commentText);
								
								// Populated the comment. Done here.
								return;
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Error getting comments", e);
			}
		}
	}
	
	/**
	 * Convert a VideoEntry object to a VideoInfo object.
	 * 
	 * @param videoEntry	The VideoEntry objec to convert
	 * @return VideoInfo or null if cannot be converted
	 */
	private VideoInfo convertVideoEntry(VideoEntry videoEntry)
	{
		// build a video info object
		try
		{
			VideoInfo videoInfo = new VideoInfo();
			
			// need to have an ID
			Matcher idMatcher = idPattern.matcher(videoEntry.getId());
			if (idMatcher.matches())
			{
				// set basic fields
				String id = idMatcher.group(1);
				VideoInfo.Identifier identifier = new VideoInfo.Identifier(VideoPlayer.YOUTUBE, id);
				videoInfo.setIdentifier(identifier);
				videoInfo.setTitle(videoEntry.getTitle().getPlainText());
				
				// get the largest thumbnail
				YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
				if (mediaGroup != null)
				{
					List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
					int largestThumbnailWidth = 0;
					String largestThumbnailUrl = "";
					for (MediaThumbnail thumbnail : thumbnails)
					{
						if (thumbnail.getWidth() > largestThumbnailWidth)
						{
							largestThumbnailUrl = thumbnail.getUrl();
							largestThumbnailWidth = thumbnail.getWidth();
						}
					}
					videoInfo.setThumbnailUrl(largestThumbnailUrl);
				}
				else
				{
					logger.debug("No media group found");
				}
				
				// get view count
				YtStatistics stats = videoEntry.getStatistics();
				if (stats != null)
				{
					videoInfo.setViews((int)stats.getViewCount());
				}
				else
				{
					logger.debug("No statistics");
				}
				
				// get the URL of the comment feed
				Comments comments = videoEntry.getComments();
				if (comments != null)
				{
					@SuppressWarnings("rawtypes")
					FeedLink commentFeedLink = comments.getFeedLink();
					String commentFeedUrl = commentFeedLink.getHref();
					
					if (commentFeedUrl != null)
					{
						YoutubeVideoInfo youtubeVideoInfo = new YoutubeVideoInfo();
						youtubeVideoInfo.setIdentifier(identifier);
						youtubeVideoInfo.setCommentFeedUrl(commentFeedUrl);
						videoInfo.setYoutubeVideoInfo(youtubeVideoInfo);
					}
				}
				else
				{
					logger.debug("No comments");
				}
				
				return videoInfo;
			}
			else
			{
				logger.debug("Couldn't find id in " + videoEntry.getId());
				return null;
			}
		}
		catch (Exception e)
		{
			// log error and return null
			logger.error("Error creating video info", e);
			return null;
		}
	}
}
