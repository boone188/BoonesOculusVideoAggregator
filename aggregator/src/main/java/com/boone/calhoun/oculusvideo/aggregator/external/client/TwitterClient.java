package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.boone.calhoun.oculusvideo.aggregator.data.TwitterVideoInfo;
import com.boone.calhoun.oculusvideo.aggregator.data.VideoInfo;

public class TwitterClient 
{
	private static Logger logger = LogManager.getLogger(TwitterClient.class);
	
	private Twitter twitter = null;
	
	public TwitterClient()
	{
		twitter = TwitterFactory.getSingleton();
	}
	
	public TwitterVideoInfo getTwitterInfoForVideo(VideoInfo.Identifier identifier) throws TwitterException
	{
		TwitterVideoInfo twitterVideoInfo = new TwitterVideoInfo();
		
		Query query = new Query();
		query.setQuery(identifier.getId());
		query.setCount(200); // the max is 200
		logger.debug("Performing Twitter query: " + query.toString());
		
		QueryResult queryResult = twitter.search(query);
		List<Status> tweets = queryResult.getTweets();

		int topTweetScore = 0;
		String topTweetText = null;
		String topTweetUserName = null;
		for (Status tweet : tweets)
		{
			int score = tweet.getFavoriteCount() + tweet.getRetweetCount();
			if (score > topTweetScore)
			{
				topTweetText = tweet.getText();
				topTweetUserName = tweet.getUser().getName();
				topTweetScore = score;
			}
		}
		
		twitterVideoInfo.setIdentifier(identifier);
		twitterVideoInfo.setNumberOfTweets(tweets.size());
		twitterVideoInfo.setTopTweetText(topTweetText);
		twitterVideoInfo.setTopTweetUserName(topTweetUserName);
		twitterVideoInfo.setTopTweetScore(topTweetScore);
		
		logger.debug("Generated tweet info: " + twitterVideoInfo);
		
		return twitterVideoInfo;
	}
}
