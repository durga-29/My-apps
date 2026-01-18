package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

@Service
public class CognitoClient {

	public AWSCognitoIdentityProvider client;
	public final String clientId = "";
	public final String userPool = "us-east-1_y9fF7Pj4x";

	public CognitoClient() {
		super();
		AWSCredentials cred = new BasicAWSCredentials("",
				"");
		
		AWSCredentialsProvider credProvider = new AWSStaticCredentialsProvider(cred);
		client = AWSCognitoIdentityProviderClientBuilder.standard().withCredentials(credProvider)
				.withEndpointConfiguration(getEndpointConfiguration("https://cognito-idp.us-east-1.amazonaws.com"))
				.build();

	}

	public String getClientId() {
		return clientId;
	}

	public String getUserPool() {
		return userPool;
	}

	private AwsClientBuilder.EndpointConfiguration getEndpointConfiguration(String url) {
		return new AwsClientBuilder.EndpointConfiguration(url, "us-east-1");
	}

}
