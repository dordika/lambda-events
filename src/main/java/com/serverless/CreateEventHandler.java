package com.serverless;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.Event;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

public class CreateEventHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		logger.info("received: { " + input.toString() + " }");

		try {
			JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
			Event event = new Event();
			event.setAppVersion(body.get("appVersion").asText());
			event.setInstance(body.get("instance").asText());
			event.setEvent(body.get("event").asText());
			event.setSessionIdentifier(body.get("sessionIdentifier").asText());
			event.setDateCreated(new Date());

			event.setEventValue(body.get("eventValue").textValue());

			event.save();

			// Successful response
			return ApiGatewayResponse.builder()
					.setStatusCode(200)
					.setObjectBody(event)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
					.build();

		} catch (Exception ex) {
			logger.error("Error in saving event ", ex);

			// send the error response back
			Response responseBody = new Response("Error { " + ex.getMessage() +  " }in saving person: ", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
					.build();
		}
	}
}
