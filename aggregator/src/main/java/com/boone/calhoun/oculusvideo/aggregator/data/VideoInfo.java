package com.boone.calhoun.oculusvideo.aggregator.data;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class VideoInfo 
{
	public static class Identifier
	{
		private VideoPlayer playerType = null;
		private String id = null;
		
		public Identifier() {}
		
		public Identifier(VideoPlayer playerType, String id)
		{
			this.playerType = playerType;
			this.id = id;
		}
		
		public String toString()
		{
			return new ToStringBuilder(this)
				.append("playerType", playerType)
				.append("id", id)
				.toString();
		}
		
		public int hashCode()
		{
			return new HashCodeBuilder()
				.append(playerType)
				.append(id)
				.toHashCode();
		}
		
		public boolean equals(Object other)
		{
			if (other != null && other instanceof Identifier)
			{
				Identifier that = (Identifier) other;
				return new EqualsBuilder()
					.append(this.playerType, that.playerType)
					.append(this.id, that.id)
					.isEquals();
			}
			
			return false;
		}
		
		public VideoPlayer getPlayerType()
		{
			return playerType;
		}
		public void setPlayerType(VideoPlayer playerType)
		{
			this.playerType = playerType;
		}
		
		public String getId()
		{
			return id;
		}
		public void setId(String id)
		{
			this.id = id;
		}
	}
	
	private Identifier identifier;
	private String title = null;
	private String thumbnailUrl = null;
	private int views = 0;
	private int rankForDay = Integer.MAX_VALUE;
	private int rankForWeek = Integer.MAX_VALUE;
	private Date uploadDate = null;
	private RedditVideoInfo redditVideoInfo = null;
	private TwitterVideoInfo twitterVideoInfo = null;
	private YoutubeVideoInfo youtubeVideoInfo = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("identifier", identifier)
			.append("title", title)
			.append("thumbnailUrl", thumbnailUrl)
			.append("view", views)
			.append("rankForDay", rankForDay)
			.append("rankForWeek", rankForWeek)
			.append("uploadDate", uploadDate)
			.append("redditVideoInfo", redditVideoInfo)
			.append("twitterVideoInfo", twitterVideoInfo)
			.append("youtubeVideoInfo", youtubeVideoInfo)
			.toString();
	}
	
	public int hashCode()
	{
		return identifier.hashCode();
	}
	
	public boolean equals(Object other)
	{
		return identifier.equals(other);
	}
	
	public static class DayRankComparator implements Comparator<VideoInfo>
	{
		public int compare(VideoInfo videoInfo1, VideoInfo videoInfo2) 
		{
			return Integer.compare(videoInfo1.getRankForDay(), videoInfo2.getRankForDay());
		}
	}
	
	public static class WeekRankComparator implements Comparator<VideoInfo>
	{
		public int compare(VideoInfo videoInfo1, VideoInfo videoInfo2) 
		{
			return Integer.compare(videoInfo1.getRankForWeek(), videoInfo2.getRankForWeek());
		}
	}
	
	public Identifier getIdentifier()
	{
		return identifier;
	}
	public void setIdentifier(Identifier identifier)
	{
		this.identifier = identifier;
	}
	
	public String getTitle() 
	{
		return title;
	}
	public void setTitle(String title) 
	{
		this.title = title;
	}
	
	public String getThumbnailUrl() 
	{
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) 
	{
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public int getViews() 
	{
		return views;
	}
	public void setViews(int views) 
	{
		this.views = views;
	}
	
	public int getRankForDay()
	{
		return rankForDay;
	}
	public void setRankForDay(int rankForDay)
	{
		this.rankForDay = rankForDay;
	}
	
	public int getRankForWeek()
	{
		return rankForWeek;
	}
	public void setRankForWeek(int rankForWeek)
	{
		this.rankForWeek = rankForWeek;
	}
	
	public Date getUploadDate()
	{
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate)
	{
		this.uploadDate = uploadDate;
	}
	
	public RedditVideoInfo getRedditVideoInfo()
	{
		return redditVideoInfo;
	}
	public void setRedditVideoInfo(RedditVideoInfo redditVideoInfo)
	{
		this.redditVideoInfo = redditVideoInfo;
	}
	
	public TwitterVideoInfo getTwitterVideoInfo()
	{
		return twitterVideoInfo;
	}
	public void setTwitterVideoInfo(TwitterVideoInfo twitterVideoInfo)
	{
		this.twitterVideoInfo = twitterVideoInfo;
	}

	public YoutubeVideoInfo getYoutubeVideoInfo()
	{
		return youtubeVideoInfo;
	}
	public void setYoutubeVideoInfo(YoutubeVideoInfo youtubeVideoInfo)
	{
		this.youtubeVideoInfo = youtubeVideoInfo;
	}
}
