package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.constants.FleetConstant;
import com.example.demo.pojo.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FleetService {


	@Autowired
	RestTemplate restTemplate;



	public void postDataToAPI(Request request) throws JsonProcessingException {


		// Create the JSON data as a Map
	       Map<String, Object> jsonRequest = new LinkedHashMap<>();
	       jsonRequest.put("firstName", request.getName());
	       jsonRequest.put("email", request.getEmail());
	       jsonRequest.put("password", "xxxxxxxx");
	       jsonRequest.put("orgId", request.getOrgId());

	       Map<String, Object> jsonRequestRoles = new LinkedHashMap<>();
	       jsonRequestRoles.put("app", request.getConnectedApp().getApp());
	       jsonRequestRoles.put("role", request.getConnectedApp().getRole());

	       ObjectMapper objectMapper = new ObjectMapper();
	       jsonRequest.put("roles", objectMapper.writeValueAsString(jsonRequestRoles));

	       // Set up request headers
	       HttpHeaders headers = new HttpHeaders();
	       headers.setContentType(MediaType.APPLICATION_JSON);

	       // Create an HttpEntity with the request body and headers
	       HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(jsonRequest, headers);

	       // Make the POST request
	       ResponseEntity<String> response = restTemplate.postForEntity(FleetConstant.FLEET_SERVER_URL, requestEntity, String.class);

	       // Handle the response as needed
	       if (response.getStatusCode().is2xxSuccessful()) {
	           System.out.println("POST request was successful.");
	           String responseBody = response.getBody();
	           System.out.println("Response Body: " + responseBody);
	       } else {
	           System.err.println("POST request failed with status code: " + response.getStatusCodeValue());
	       }


	}


}
