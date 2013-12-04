package com.boone.calhoun.oculusvideo.aggregator.external.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;


public class DynamoDbClient 
{
	private static String TABLE_NAME = "hot_oculus_videos";
	private static String KEY_NAME = "sort_type";
	private static String DATA_NAME = "video_info";
	
	AmazonDynamoDBClient dynamoClient = null;
	
	public DynamoDbClient() throws IOException
	{
		String accessKey = System.getenv("AWS_ACCESS_KEY");
		String secretKey = System.getenv("AWS_SECRET_KEY");
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        dynamoClient = new AmazonDynamoDBClient(credentials);
        
        String endpoint = System.getenv("AWS_ENDPOINT");
        dynamoClient.setEndpoint(endpoint);
	}
	
	public void writeData(String sortType, String data)
	{
		Map<String, AttributeValue> attributeMap = new HashMap<String, AttributeValue>();
		attributeMap.put(KEY_NAME, new AttributeValue().withS(sortType));
		attributeMap.put(DATA_NAME, new AttributeValue().withS(data));
		
		PutItemRequest request = new PutItemRequest()
			.withTableName(TABLE_NAME)
			.withItem(attributeMap);
		
		dynamoClient.putItem(request);
	}
}
