package com.serverless;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import org.apache.log4j.Logger;

import java.util.Map;

public class SendNotificationHandler implements RequestHandler<DynamodbEvent, Void> {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		logger.info("received dynamoDBEvent stream");

		try {
			for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

				if (record == null) {
					continue;
				}

				if (record.getEventName().equals("INSERT")) {
					Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
					logger.info("looping record: " + newImage.toString());

					if (newImage.containsKey("event")) {
						AttributeValue event = newImage.get("event");
						logger.info("event to string: " + event.getS());
						if (event.getS().contains("exception")) {

						}
					}
				}
			}

			return null;

		} catch (Exception ex) {
			logger.error("Error in saving event ", ex);
			return null;
		}
	}
}
