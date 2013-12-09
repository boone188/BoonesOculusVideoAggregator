package com.boone.calhoun.oculusvideo.aggregator.data;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TwitterVideoInfo 
{
	private transient VideoInfo.Identifier identifier;
	private int numberOfTweets = 0;
	private String topTweetText = null;
	private String topTweetUserName = null;
	private int topTweetScore = 0;

	public String toString()
	{
		return new ToStringBuilder(this)
			.append("identier", identifier)
			.append("numberOfTweet", numberOfTweets)
			.append("topTweetUserText", topTweetText)
			.append("topTweetUserName", topTweetUserName)
			.append("topTweetScore", topTweetScore)
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

	public int getNumberOfTweets() 
	{
		return numberOfTweets;
	}
	public void setNumberOfTweets(int numberOfTweets) 
	{
		this.numberOfTweets = numberOfTweets;
	}

	public String getTopTweetText() 
	{
		return topTweetText;
	}
	public void setTopTweetText(String topTweetText) 
	{
		this.topTweetText = topTweetText;
	}

	public String getTopTweetUserName() 
	{
		return topTweetUserName;
	}
	public void setTopTweetUserName(String topTweetUserName) 
	{
		this.topTweetUserName = topTweetUserName;
	}
	
	public int getTopTweetScore()
	{
		return topTweetScore;
	}
	public void setTopTweetScore(int topTweetScore)
	{
		this.topTweetScore = topTweetScore;
	}
}
