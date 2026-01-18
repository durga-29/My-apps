package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.RevokeTokenRequest;
import com.amazonaws.services.cognitoidp.model.RevokeTokenResult;
import com.example.jwtvalidate.CustomException;


@Service
public class CommonService {

	private  AWSCognitoIdentityProvider client;
	private  CognitoClient cognitoClient;


	@Autowired
	public CommonService(CognitoClient cognitoClient) {
		this.cognitoClient =cognitoClient;
		client =cognitoClient.client;
	}

	public void signOut(String refreshToken) throws CustomException {

		// Clear the user's session and any client-side tokens

       RevokeTokenRequest revokeTokenRequest =
    		   new RevokeTokenRequest()
    		   		.withToken(refreshToken)
    		   		.withClientId(cognitoClient.clientId);

        try {
            // Call the RevokeToken API to revoke the refresh token
            RevokeTokenResult result = client.revokeToken(revokeTokenRequest);
            System.out.println(result.getSdkHttpMetadata().getHttpStatusCode());

        } catch (Exception e) {
        	throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Revocation failed ", e.getMessage());
        }
    }



}
