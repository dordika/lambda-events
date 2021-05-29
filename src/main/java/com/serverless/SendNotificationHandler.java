package com.serverless;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.log4j.Logger;

import java.util.Map;

public class SendNotificationHandler implements RequestHandler<DynamodbEvent, Void> {

	private final Logger logger = Logger.getLogger(this.getClass());

	//send email tutorial https://docs.aws.amazon.com/ses/latest/DeveloperGuide/send-using-sdk-java.html

	// Replace sender@example.com with your "From" address.
	// This address must be verified with Amazon SES.
	static final String FROM = "dor.dika@stud.uniroma3.it";
	// Replace recipient@example.com with a "To" address. If your account
	// is still in the sandbox, this address must be verified.
	static final String TO = "dorjan.dika.de@gmail.com";

	// The subject line for the email.
	static final String SUBJECT = "AWS Lambda mail Test (exception event)";

	// The HTML body for the email.
	static final String HTMLBODY = "<h1>AWS Lambda test (Exception event)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
			+ "AWS SDK for Java</a> as part of a thesis project.";

	// The email body for recipients with non-HTML email clients.
	static final String TEXTBODY = "This email was sent through Amazon SES "
			+ "using the AWS SDK for Java.";

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
							sendEmail(newImage.get("eventValue").getS());
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

	private void sendEmail(String content) {

		logger.info("Starting sending email with content: " + content );

		String messageHTMLBody = HTMLBODY.concat("\n").concat(content);
		String messageTextBody = TEXTBODY.concat("\n").concat(content);

		try {
			AmazonSimpleEmailService client =
					AmazonSimpleEmailServiceClientBuilder.standard()
							.withRegion(Regions.US_EAST_1).build();
			SendEmailRequest request = new SendEmailRequest()
					.withDestination(
							new Destination().withToAddresses(TO))
					.withMessage(new Message()
							.withBody(new Body()
									.withHtml(new Content()
											.withCharset("UTF-8").withData(messageHTMLBody))
									.withText(new Content()
											.withCharset("UTF-8").withData(messageTextBody)))
							.withSubject(new Content()
									.withCharset("UTF-8").withData(SUBJECT)))
					.withSource(FROM);
			client.sendEmail(request);
			logger.info("Email sent!");
		} catch (Exception ex) {
			logger.error("The email was not sent. Error message: ", ex);
		}
	}
}
