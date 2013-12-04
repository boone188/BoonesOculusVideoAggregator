package com.boone.calhoun.oculusvideo.aggregator;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *  What you've got here is an application that aggregates videos related to the Oculus Rift.
 *  It collects information about the videos and then creates lists with containing the
 *  video info. It publishes these lists to a data store, where they can be grabbed quickly
 *  later on.
 */
public class OculusVideoAggregatorApplication 
{
	private static Logger logger = LogManager.getLogger(OculusVideoAggregatorApplication.class);
	
	/*
	 *  Entrance point
	 */
    public static void main( String[] args ) throws IOException
    {
    	// How often should videos be aggregated
    	int aggregationIntervalMinutes = Integer.parseInt(System.getenv("VIDEO_AGGREGATION_INTERVAL_MINUTES"));
    	
    	// Start up a repeating task
    	Timer aggregationTimer = new Timer();
    	aggregationTimer.schedule(
			new TimerTask()
			{
				public void run()
				{
					try
					{
						// Aggregate videos
						new OculusVideoAggregator().aggregateVideos();
					}
					catch (Exception e)
					{
						logger.error("Error aggregating videos", e);
					}
				}
			}, 
			0, // Initial delay
			aggregationIntervalMinutes * 60 * 1000  // Interval
    	);
    }
    
    
}
