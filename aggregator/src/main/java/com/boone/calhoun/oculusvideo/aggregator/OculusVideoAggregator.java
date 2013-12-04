package com.boone.calhoun.oculusvideo.aggregator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.boone.calhoun.oculusvideo.aggregator.data.RedditVideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.TwitterVideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoPlayer;
import com.boone.calhoun.oculusvideo.aggregator.external.client.CloudWatchClient;
import com.boone.calhoun.oculusvideo.aggregator.external.client.DynamoDbClient;
import com.boone.calhoun.oculusvideo.aggregator.external.client.RedditClient;
import com.boone.calhoun.oculusvideo.aggregator.external.client.RedditClient.ListType;
import com.boone.calhoun.oculusvideo.aggregator.external.client.TwitterClient;
import com.boone.calhoun.oculusvideo.aggregator.external.client.VideoPlayersClient;
import com.boone.calhoun.oculusvideo.aggregator.external.client.VideoPlayersClient.OrderBy;
import com.boone.calhoun.oculusvideo.aggregator.external.client.VideoPlayersClient.TimeRange;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This is where all the good stuff happens.
 *
 */
public class OculusVideoAggregator 
{
	Logger logger = LogManager.getLogger(OculusVideoAggregator.class);
	
	public void aggregateVideos() throws IOException
    {
    	// How many videos should be collected for the day
    	int numVideosForDay = Integer.parseInt(System.getenv("NUM_VIDEOS_FOR_DAY"));
    	
    	// How many videos should be collected for the week
    	int numVideosForWeek = Integer.parseInt(System.getenv("NUM_VIDEOS_FOR_WEEK"));
    	
        // Build clients
        VideoPlayersClient videoPlayersClient = new VideoPlayersClient();
        RedditClient redditClient = new RedditClient();
        TwitterClient twitterClient = new TwitterClient();
        DynamoDbClient dynamoDbClient = new DynamoDbClient();
        CloudWatchClient cloudWatchClient = new CloudWatchClient();
        
        // Map of video identifiers to video info for all the new videos that will be added
        Map<VideoInfo.Identifier, VideoInfo> newVideoInfoMap = new HashMap<VideoInfo.Identifier, VideoInfo>();
        
        // Get todays most relevant videos from Youtube
        try
        {
        	List<VideoInfo> todaysYoutubeVideos = 
        			videoPlayersClient.getVideoList(VideoPlayer.YOUTUBE, "oculus rift", numVideosForDay, OrderBy.RELEVANCE, TimeRange.DAY);
        	
        	// Traverse the list of todays videos
        	for (int i = 0; i < todaysYoutubeVideos.size(); i++)
        	{
        		VideoInfo videoInfo = todaysYoutubeVideos.get(i);
        		VideoInfo.Identifier identifier = videoInfo.getIdentifier();
        		
        		if (newVideoInfoMap.containsKey(identifier))
        		{
        			// The video's rank is its position in the list
        			newVideoInfoMap.get(identifier).setRankForDay(i);
        		}
        		else
        		{
        			videoInfo.setRankForDay(i);
        			newVideoInfoMap.put(identifier, videoInfo);
        		}
        	}
        }
        catch (Exception e)
        {
        	logger.error("Error getting today's video list from Youtube", e);
        }
        
        // Get this week's most relevant videos from Youtube
        try
        {
        	List<VideoInfo> thisWeeksYoutubeVideos = 
        			videoPlayersClient.getVideoList(VideoPlayer.YOUTUBE, "oculus rift", numVideosForWeek, OrderBy.RELEVANCE, TimeRange.WEEK);
        	
        	// Traverse the list of todays videos
        	for (int i = 0; i < thisWeeksYoutubeVideos.size(); i++)
        	{
        		VideoInfo videoInfo = thisWeeksYoutubeVideos.get(i);
        		VideoInfo.Identifier identifier = videoInfo.getIdentifier();
        		
        		if (newVideoInfoMap.containsKey(identifier))
        		{
        			// The video's rank is its position in the list
        			newVideoInfoMap.get(identifier).setRankForWeek(i);
        		}
        		else
        		{
        			videoInfo.setRankForWeek(i);
        			newVideoInfoMap.put(identifier, videoInfo);
        		}
        	}
        }
        catch (Exception e)
        {
        	logger.error("Error getting this week's video list from Youtube", e);
        }
        
        // Get today's hot youtube videos from Reddit
        try
        {
        	List<RedditVideoInfo> todaysTopRedditVideos = 
        			redditClient.getVideoList(VideoPlayer.YOUTUBE, ListType.HOT, RedditClient.TimeRange.DAY, numVideosForDay);
        	
        	// Traverse the list in reverse order, in case there are repeat listings on Reddit
        	for (int i = todaysTopRedditVideos.size()-1; i >= 0; i--)
        	{
        		RedditVideoInfo redditVideoInfo = todaysTopRedditVideos.get(i);
        		VideoInfo.Identifier identifier = redditVideoInfo.getIdentifier();
        		
        		if (newVideoInfoMap.containsKey(identifier))
        		{
        			newVideoInfoMap.get(identifier).setRedditVideoInfo(redditVideoInfo);
        		}
        		else
        		{
        			try
        			{
	        			VideoInfo videoInfo = videoPlayersClient.getVideoInfo(identifier);
	        			videoInfo.setRedditVideoInfo(redditVideoInfo);
	        			videoInfo.setRankForDay(i);
	        			newVideoInfoMap.put(identifier, videoInfo);
        			}
        			catch (Exception e)
        			{
        				logger.error("Error getting youtube video found on Reddit", e);
        			}
        		}
        	}
        }
        catch (Exception e)
        {
        	logger.error("Error getting videos from Reddit", e);
        }
        
        // Get this week's hot Youtube videos from Reddit
        try
        {
        	List<RedditVideoInfo> thisWeeksTopRedditVideos = 
        			redditClient.getVideoList(VideoPlayer.YOUTUBE, ListType.HOT, RedditClient.TimeRange.WEEK, numVideosForWeek);
        	
        	// Traverse the list in reverse order, in case there are repeat listings on Reddit
        	for (int i = thisWeeksTopRedditVideos.size()-1; i >= 0; i--)
        	{
        		RedditVideoInfo redditVideoInfo = thisWeeksTopRedditVideos.get(i);
        		VideoInfo.Identifier identifier = redditVideoInfo.getIdentifier();
        		
        		if (newVideoInfoMap.containsKey(identifier))
        		{
        			newVideoInfoMap.get(identifier).setRedditVideoInfo(redditVideoInfo);
        		}
        		else
        		{
        			try
        			{
	        			VideoInfo videoInfo = videoPlayersClient.getVideoInfo(identifier);
	        			videoInfo.setRedditVideoInfo(redditVideoInfo);
	        			videoInfo.setRankForWeek(i);
	        			newVideoInfoMap.put(identifier, videoInfo);
        			}
        			catch (Exception e)
        			{
        				logger.error("Error getting youtube video found on Reddit", e);
        			}
        		}
        	}
        }
        catch (Exception e)
        {
        	logger.error("Error getting videos from Reddit", e);
        }
        
        // Get Twitter info for the new videos
        for (VideoInfo.Identifier identifier : newVideoInfoMap.keySet())
        {
        	try
        	{
        		TwitterVideoInfo twitterVideoInfo = twitterClient.getTwitterInfoForVideo(identifier);
        		if (twitterVideoInfo != null && twitterVideoInfo.getNumberOfTweets() > 0)
        		{
        			newVideoInfoMap.get(identifier).setTwitterVideoInfo(twitterVideoInfo);
        		}
        	}
        	catch (Exception e)
        	{
        		logger.error("Error getting Twitter info for video", e);
        	}
        }
        
        Gson gson = new Gson();
        Type videoInfoListType = new TypeToken<List<VideoInfo>>(){}.getType();
        
        // Sort by today's rank and store
        try
        {
        	// Sort by day rank
	        List<VideoInfo> todaysHotVideos = new ArrayList<VideoInfo>(newVideoInfoMap.values());
	        Collections.sort(todaysHotVideos, new VideoInfo.DayRankComparator());
	        
	        // Truncate to the desired number of videos
	        todaysHotVideos = todaysHotVideos.subList(0, numVideosForWeek);
	        
	        // Store in Dynamo
	        String todaysHotVideosJson = gson.toJson(todaysHotVideos, videoInfoListType);
	        dynamoDbClient.writeData("sort=hot&time=day", todaysHotVideosJson);
	        
	        // Publish metric
	        cloudWatchClient.publishCount("DailyVideoCount", todaysHotVideos.size());
        }
        catch (Exception e)
        {
        	logger.error("Error writing today's hot videos to dynamo", e);
        }
        
        // Sort by this week's rank and store
        try
        {
        	// Sort by week rank
	        List<VideoInfo> thisWeeksHotVideos = new ArrayList<VideoInfo>(newVideoInfoMap.values());
	        Collections.sort(thisWeeksHotVideos, new VideoInfo.WeekRankComparator());
	        
	        // Truncate to the desired number of videos
	        thisWeeksHotVideos = thisWeeksHotVideos.subList(0, numVideosForWeek);
	        
	        // Store in Dynamo
	        String thisWeeksHotVideosJson = gson.toJson(thisWeeksHotVideos, videoInfoListType);
	        dynamoDbClient.writeData("sort=hot&time=week", thisWeeksHotVideosJson);
	        
	        // Publish metric
	        cloudWatchClient.publishCount("WeeklyVideoCount", thisWeeksHotVideos.size());
        }
        catch (Exception e)
        {
        	logger.error("Error writing this week's hot videos to dynamo", e);
        }
    }
}
