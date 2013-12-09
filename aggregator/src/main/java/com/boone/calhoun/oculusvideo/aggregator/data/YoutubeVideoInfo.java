package com.boone.calhoun.oculusvideo.aggregator.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class YoutubeVideoInfo 
{
	private transient VideoInfo.Identifier identifier = null;;
	private transient String commentFeedUrl = null;
	private String topCommentAuthor = null;
	private String topCommentText = null;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("identifier", identifier)
			.append("commentFeedUrl", commentFeedUrl)
			.append("topCommentAuthor", topCommentAuthor)
			.append("topCommentText", topCommentText)
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
	
	public VideoInfo.Identifier getIdentifier() 
	{
		return identifier;
	}
	public void setIdentifier(VideoInfo.Identifier identifier) 
	{
		this.identifier = identifier;
	}
	
	public String getCommentFeedUrl() 
	{
		return commentFeedUrl;
	}
	public void setCommentFeedUrl(String commentFeedUrl) 
	{
		this.commentFeedUrl = commentFeedUrl;
	}
	
	public String getTopCommentAuthor() 
	{
		return topCommentAuthor;
	}
	public void setTopCommentAuthor(String topCommentAuthor) 
	{
		this.topCommentAuthor = topCommentAuthor;
	}
	
	public String getTopCommentText() 
	{
		return topCommentText;
	}
	public void setTopCommentText(String topCommentText) 
	{
		this.topCommentText = topCommentText;
	}
	
	
}
