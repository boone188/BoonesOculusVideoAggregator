#!/bin/bash
export NUM_VIDEOS_FOR_DAY='10';
export NUM_VIDEOS_FOR_WEEK='10';
export VIDEO_AGGREGATION_INTERVAL_MINUTES='15';

export AWS_ACCESS_KEY=AKIAJPGJIFISFF5XTS5A
export AWS_SECRET_KEY=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
export AWS_ENDPOINT='dynamodb.us-west-1.amazonaws.com'
export AWS_METRICS_NAMESPACE='BoonesOculusVideosData'

java -Dlog4j.configurationFile=config/log4j2.xml \
     -Dtwitter4j.oauth.consumerKey=3cd2isr2swVeDPRQwAugg \
     -Dtwitter4j.oauth.consumerSecret=NTIhcwusJ1BwfvfwTnRNa0PxU9YIT7Qp2TTvm0YfgjU \
     -Dtwitter4j.oauth.accessToken=477388887-7ZMzbrMIeGnfCklBwyL5Wh1tU6JVDXvXt9DrCpX7 \
     -Dtwitter4j.oauth.accessTokenSecret=vlBafxWqL8kh4S5uRDXmLDmS9vfKORcoIyDMT3tIiVqbM \
     -jar target/aggregator-1.0-jar-with-dependencies.jar