package com.boone.calhoun.oculusvideo.aggregator.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class RedditVideoInfo 
{
	private static int MAX_COMMENT_LENGTH = 1000;
	
	private transient VideoInfo.Identifier identifier = null;
	private String url = null;
	private int score = 0;
	private int upvotes = 0;
	private int downvotes = 0;
	private String title = null;
	private boolean over18 = false;
	private boolean isHot = false;
	private String topCommentAuthor = null;
	private String topCommentText = null;
	private int topCommentScore = 0;
	
	public String toString()
	{
		return new ToStringBuilder(this)
			.append("identifier", identifier)
			.append("url", url)
			.append("score", score)
			.append("upvotes", upvotes)
			.append("downvotes", downvotes)
			.append("title", title)
			.append("over18", over18)
			.append("isHot", isHot)
			.append("topCommentAuthor", topCommentAuthor)
			.append("topCommentText", topCommentText)
			.append("topCommentScore", topCommentScore)
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
	

	public String getUrl() 
	{
		return url;
	}

	public void setUrl(String url) 
	{
		this.url = url;
	}
	

	public int getScore() 
	{
		return score;
	}

	public void setScore(int score) 
	{
		this.score = score;
	}
	

	public int getUpvotes() 
	{
		return upvotes;
	}

	public void setUpvotes(int upvotes) 
	{
		this.upvotes = upvotes;
	}
	

	public int getDownvotes() 
	{
		return downvotes;
	}

	public void setDownvotes(int downvotes) 
	{
		this.downvotes = downvotes;
	}
	

	public String getTitle() 
	{
		return title;
	}

	public void setTitle(String title) 
	{
		this.title = title;
	}
	

	public boolean isOver18() 
	{
		return over18;
	}

	public void setOver18(boolean over18) 
	{
		this.over18 = over18;
	}
	

	public boolean isHot() 
	{
		return isHot;
	}

	public void setHot(boolean isHot) 
	{
		this.isHot = isHot;
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
		// Truncate the comment to the maximum allowed size
		if (topCommentText.length() >= MAX_COMMENT_LENGTH)
		{
			topCommentText = topCommentText.substring(0, MAX_COMMENT_LENGTH) + "...";
		}
		
		this.topCommentText = topCommentText;
	}
	
	public int getTopCommentScore()
	{
		return topCommentScore;
	}
	public void setTopCommentScore(int topCommentScore)
	{
		this.topCommentScore = topCommentScore;
	}
}
