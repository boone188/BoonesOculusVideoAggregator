package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.util.List;

import com.boone.calhoun.oculusvideo.aggregator.data.VideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoPlayer;
import com.google.gdata.client.youtube.YouTubeQuery;

public class VideoPlayersClient 
{
	/**
	 * Used to specify how to order the results of a query.
	 */
	public enum OrderBy
	{
		RELEVANCE (YouTubeQuery.OrderBy.RELEVANCE),
		VIEW_COUNT (YouTubeQuery.OrderBy.VIEW_COUNT);
		
		private YouTubeQuery.OrderBy youtubeOrderBy;
		
		OrderBy(YouTubeQuery.OrderBy youtubeOrderBy)
		{
			this.youtubeOrderBy = youtubeOrderBy;
		}
	}
	
	public enum TimeRange
	{
		DAY (YouTubeQuery.Time.TODAY),
		WEEK (YouTubeQuery.Time.THIS_WEEK);
		
		private YouTubeQuery.Time youtubeTimeRange;
		
		TimeRange(YouTubeQuery.Time youtubeTimeRange)
		{
			this.youtubeTimeRange = youtubeTimeRange;
		}
	}
	
	private YoutubeClient youtubeClient = null;
	
	public VideoPlayersClient()
	{
		youtubeClient = new YoutubeClient();
	}
	
	/**
	 * Get a list of videos from a video player based on query parameters.
	 * 
	 * @param playerType	The type of the player to query
	 * @param queryText		The text to search for
	 * @param maxCount		The maximum number of videos to return
	 * @param orderBy		How to order the results
	 * @param timeRange		The time range of videos to consider
	 * @return List<VideoInfo>
	 * @throws Exception if unable to query for videos
	 */
	public List<VideoInfo> getVideoList(VideoPlayer playerType, String queryText, int maxCount, OrderBy orderBy, TimeRange timeRange) throws Exception
	{
		if (playerType == null)
		{
			throw new IllegalArgumentException("playerType cannot be null");
		}
		
		if (VideoPlayer.YOUTUBE.equals(playerType))
		{
			return youtubeClient.getVideoList(queryText, maxCount, orderBy.youtubeOrderBy, timeRange.youtubeTimeRange);
		}
		else
		{
			throw new UnsupportedOperationException("Only Youtube is supported right now.");
		}
	}
	
	/**
	 * Get video info by Youtube ID.
	 * 
	 * @param identifier	The identifier of the video to retrieve
	 * @return VideoInfo or null if the video entry cannot be converted to a VideoInfo
	 * @throws Exception if unable to query for the video
	 */
	public VideoInfo getVideoInfo(VideoInfo.Identifier identifier) throws Exception
	{
		if (identifier == null)
		{
			throw new IllegalArgumentException("identifier cannot be null");
		}
		
		if (VideoPlayer.YOUTUBE.equals(identifier.getPlayerType()))
		{
			return youtubeClient.getVideoInfo(identifier.getId());
		}
		else
		{
			throw new UnsupportedOperationException("Only Youtube is supported right now.");
		}
	}
}
