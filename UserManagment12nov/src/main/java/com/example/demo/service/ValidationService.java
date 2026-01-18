package com.example.demo.service;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.example.demo.pojo.Response;
import com.example.demo.util.TokenValidationUtil;
import com.example.jwtvalidate.CustomException;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTClaimsSet;


@Service
public class ValidationService {

	private  AWSCognitoIdentityProvider client;
	private  CognitoClient cognitoClient;

	@Autowired
	TokenValidationUtil util;


	@Autowired
	public ValidationService(CognitoClient cognitoClient) {
		super();
		this.cognitoClient =cognitoClient;
		client =cognitoClient.client;
	}

	public Response validate(String email, String accessToken, String refreshToken)
			throws CustomException {

		Response response = new Response();

	    System.out.println("Validating signature and retreiving claimset");
		JWTClaimsSet jwtClaimsSet = util.validateAWSJwtToken(accessToken);

		if (jwtClaimsSet == null) {
		    System.out.println("Token is not valid (Auto-Generated new tokens)");
			return new Response(true, "Token is now valid (Auto-Generated new tokens)", refreshTokens(refreshToken));
		}

		String claimEmail;
		long claimExpiry;
		try {
			System.out.println("jwtClaimsSet: "+jwtClaimsSet);
			claimEmail = jwtClaimsSet.getStringClaim("email");
			System.out.println("claimEmail"+ claimEmail);
			claimExpiry = jwtClaimsSet.getDateClaim("exp").getTime();

		} catch (ParseException e) {
			e.printStackTrace();
	      	throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "ParseException:", e.getMessage());
		}


		if (!email.equals(claimEmail)) {
		    System.out.println("Token is invalid (Email matching failed)");
			return new Response(false, "Token is invalid (Email matching failed)", null);
		}

		if (claimExpiry < System.currentTimeMillis()) {
	        System.out.println("Checking expiry)");
			if(refreshToken!=null) {
		    System.out.println("Token is now valid (Auto-Generated new tokens)");
			response.setExtras(refreshTokens(refreshToken));
			response.setSuccess(true);
			response.setMessage("Token is now valid (Auto-Generated new tokens)");
			}else {
		        System.out.println("Token is invalid (Token is expired)");
				return new Response(false, "Token is invalid (Token is expired)", null);
			}


		} else {
		    System.out.println("Token is valid");
			response.setExtras(null);
			response.setSuccess(true);
			response.setMessage("Token is valid");
		}

		return response;

	}


	private Map<String, Object> refreshTokens(String refreshToken) {
		// TODO Auto-generated method stub
		AdminInitiateAuthRequest request = new AdminInitiateAuthRequest().withUserPoolId(cognitoClient.userPool)
				.withClientId(cognitoClient.clientId).withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
				.withAuthParameters(Collections.singletonMap("REFRESH_TOKEN", refreshToken));

		AdminInitiateAuthResult result = client.adminInitiateAuth(request);
		AuthenticationResultType resultType = result.getAuthenticationResult();

		Map<String, Object> extras = new HashMap<>();
		extras.put("idToken", resultType.getIdToken());
		extras.put("accessToken", resultType.getAccessToken());

		return extras;

	}

	public Map<String, Object> decodeToken(String idToken) throws CustomException {
        JsonObject jsonObject =TokenValidationUtil.getPayload(idToken);
        Map<String, Object> extras = new HashMap<>();
        extras.put("orgId",jsonObject.get("custom:orgId").getAsString());
        extras.put("email", jsonObject.get("email").getAsString());
        extras.put("userId", jsonObject.get("cognito:username").getAsString());
        extras.put("name", jsonObject.get("name").getAsString());
        return extras;
    }

}
