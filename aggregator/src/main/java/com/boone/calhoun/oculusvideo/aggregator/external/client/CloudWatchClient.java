package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class CloudWatchClient 
{
	private static Logger logger = LogManager.getLogger(CloudWatchClient.class);
	
	private AmazonCloudWatchClient cwClient = null;
	private String namespace = null;
	private AWSCredentials credentials = null;
	
	public CloudWatchClient()
	{
		String accessKey = System.getenv("AWS_ACCESS_KEY");
		String secretKey = System.getenv("AWS_SECRET_KEY");
		credentials = new BasicAWSCredentials(accessKey, secretKey);

        cwClient = new AmazonCloudWatchClient(credentials);
        
        namespace = System.getenv("AWS_METRICS_NAMESPACE");
	}
	
	public void publishCount(String name, double count)
	{
		try
		{
			MetricDatum metricDatum = new MetricDatum()
				.withMetricName(name)
				.withUnit(StandardUnit.Count)
				.withValue(count)
				.withTimestamp(new Date());
			
			PutMetricDataRequest request = new PutMetricDataRequest()
				.withNamespace(namespace)
				.withMetricData(metricDatum);
			
			cwClient.putMetricData(request);
		}
		catch (Exception e)
		{
			// Never fail due to publishing failure
			logger.error("Error publishing metric", e);
		}
	}
}
