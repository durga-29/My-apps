package com.example.demo.service;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChangePasswordRequest;
import com.amazonaws.services.cognitoidp.model.ChangePasswordResult;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.ConfirmSignUpRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmSignUpResult;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.SignUpResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.example.demo.pojo.ConnectedAppPojo;
import com.example.demo.pojo.Response;
import com.example.dto.RoleDto;
import com.example.dto.UserResponse;

import com.example.jwtvalidate.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private AWSCognitoIdentityProvider client;
	private CognitoClient cognitoClient;

	Gson gson = new Gson();

	@Autowired
	AdminService adminService;
	
	@Autowired
	CognitoCache cognitoCache;
	
	String clientSecret = "iumkaloa3smv4ql350la2l2osodiknlhgqstah09j03pukt3sbe";
//			"iumkaloa3smv4ql350la2l2osodiknlhgqstah09j03pukt3sbe";

	@Autowired
	public UserService(CognitoClient cognitoClient) {
		this.cognitoClient = cognitoClient;
		client = cognitoClient.client;
	}

	public Response signUp(String email, String password, String name, ConnectedAppPojo connectedApp, String orgId)
			throws CustomException {

		Map<String, String> extraMap = new HashMap<>();
		Response response = new Response();

		List<ConnectedAppPojo> connectedAppList = new ArrayList<>();
		connectedAppList.add(connectedApp);

		System.out.println("connectedApp" + connectedApp.getApp());

		String secretHash = calculateSecretHash(email, cognitoClient.clientId, clientSecret);
		System.out.println(secretHash);

		SignUpRequest request = new SignUpRequest().withClientId(cognitoClient.clientId).withUsername(email)
				.withPassword(password)
				.withUserAttributes(
						new AttributeType().withName("custom:connectedApp").withValue(gson.toJson(connectedAppList)),
						new AttributeType().withName("custom:orgId").withValue(orgId),
						new AttributeType().withName("name").withValue(name),
						new AttributeType().withName("email").withValue(email))
				.withSecretHash(secretHash); // Add SECRET_HASH to the request;

		try {

			SignUpResult result = client.signUp(request);
			response.setSuccess(true);
			response.setMessage("Verification code sent on " + email);

		} catch (UsernameExistsException e) {

			Response r = adminService.getUserDetailByMail(email);
			if (!r.getSuccess()) {
				response = r;
			} else {

				// Define the type of the target object
				Type personListType = new TypeToken<ArrayList<ConnectedAppPojo>>() {
				}.getType();

				// Convert JSON array to ArrayList of Person objects
				ArrayList<ConnectedAppPojo> connectedApps = gson
						.fromJson((String) r.getExtras().get("custom:connectedApp"), personListType);

				for (ConnectedAppPojo c : connectedApps) {
					if (c.getApp().equals(connectedApp.getApp())) {
						throw new CustomException(HttpStatus.BAD_REQUEST, "X", "User already exist for this domain");
					}
				}

				// Adding additional connectedApp to custom:connectedApps
				connectedApps.add(connectedApp);
				r = adminService.updateCustomAttribute(email, name, "custom:connectedApp", gson.toJson(connectedApps));
				if (!r.getSuccess()) {
					response = r;
				} else {
					response.setSuccess(true);
					response.setMessage("signup success");
				}
				System.out.println(r);
			}
		}

		return response;

	}

	public String calculateSecretHash(String email, String clientId, String clientSecret) {
		try {
			String message = email + clientId;

			SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKeySpec);
			byte[] rawHmac = mac.doFinal(message.getBytes());

//			   SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//		        Mac mac = Mac.getInstance("HmacSHA256");
//		        mac.init(secretKeySpec);
//		        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

			return Base64.getEncoder().encodeToString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Error calculating SECRET_HASH", e);
		}
	}

	public ConfirmSignUpResult confirmSignUp(String email, String confirmationCode) {
		String secretHash = calculateSecretHash(email, cognitoClient.clientId, clientSecret);

		ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest().withClientId(cognitoClient.clientId)
				.withUsername(email).withConfirmationCode(confirmationCode).withSecretHash(secretHash);

		return client.confirmSignUp(confirmSignUpRequest);
	}

	public Response resetUserPassword(String email, String password) {

		Map<String, String> extraMap = new HashMap<>();

		return adminService.resetUserPassword(email, password);

	}

	public Response signIn(String email, String password, ConnectedAppPojo connectedApp) {
		return initiateAuth(email, password, connectedApp);

	}

	public Response initiateAuth(String email, String password, ConnectedAppPojo connectedApp) {

		Response response = new Response();
		String secretHash = calculateSecretHash(email, cognitoClient.clientId, clientSecret);
		System.out.println(" user service signin " + secretHash);

		AuthFlowType authFlowType = AuthFlowType.USER_PASSWORD_AUTH;
		Map<String, String> authParameters = new HashMap<>();
		authParameters.put("USERNAME", email);
		authParameters.put("PASSWORD", password);
		authParameters.put("SECRET_HASH", secretHash);

		InitiateAuthRequest authRequest = new InitiateAuthRequest().withAuthFlow(authFlowType)
				.withClientId(cognitoClient.clientId).withAuthParameters(authParameters);

		try {
			InitiateAuthResult authResult = client.initiateAuth(authRequest);

			Response r = adminService.getUserDetailByMail(email);
			System.out.println(r);
			if (!r.getSuccess()) {
				response = r;
			} else {

				// Define the type of the target object
				Type personListType = new TypeToken<ArrayList<ConnectedAppPojo>>() {
				}.getType();

				// Convert JSON array to ArrayList of Person objects
				System.out.println(r.getExtras());

				System.out.println(r.getExtras().get("custom:connectedApp"));
				ArrayList<ConnectedAppPojo> connectedApps = gson
						.fromJson((String) r.getExtras().get("custom:connectedApp"), personListType);

				boolean isConnectedAppAlreadyExist = false;
				for (ConnectedAppPojo c : connectedApps) {
					if (c.getApp().equals(connectedApp.getApp())) {
						isConnectedAppAlreadyExist = true;
					}
				}

				if (isConnectedAppAlreadyExist) {

					AuthenticationResultType resultType = authResult.getAuthenticationResult();
					response.setSuccess(true);
					response.setMessage("signin success to domain " + connectedApp.getApp());
					response.setExtras(new LinkedHashMap<String, Object>() {
						{
							put("idToken", resultType.getIdToken());
							put("accessToken", resultType.getAccessToken());
							put("refreshToken", resultType.getRefreshToken());
							put("message", "Successfully login");
						}
					});
					String idToken = resultType.getIdToken();
					String[] parts = idToken.split("\\.");
					String payload1 = new String(Base64.getUrlDecoder().decode(parts[1]));
					JsonObject json = JsonParser.parseString(payload1).getAsJsonObject();
					String username = json.get("cognito:username").getAsString();
					System.out.println("Cognito username = " + username);

					cognitoCache.save(resultType.getRefreshToken(), username);

				} else {
					response.setSuccess(false);
					response.setMessage("Please first signup for " + connectedApp.getApp() + " Domain");
				}

			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Sign-in failed");
			response.setExtras(new LinkedHashMap<String, Object>() {
				{
					put("errorType", e.getClass().getName());
					put("errorMessage", e.getMessage());
				}
			});

		}

		return response;

	}
	public String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}
	public Map<String, String> getUserAttributes(String accessToken) {

		Map<String, String> extraMap = new HashMap<>();

		GetUserRequest getUserRequest = new GetUserRequest().withAccessToken(accessToken);

		// Retrieve user information
		GetUserResult getUserResult = client.getUser(getUserRequest);

		// Extract user attributes from the result
		List<AttributeType> userAttributes = getUserResult.getUserAttributes();
		for (AttributeType attributeType : userAttributes) {
			extraMap.put(attributeType.getName(), attributeType.getValue());
		}

		return extraMap;
	}

	public ForgotPasswordResult forgotPassword(String email) {
		// TODO Auto-generated method stub
		String secretHash = calculateSecretHash(email, cognitoClient.clientId, clientSecret);
		// Create the forgot password request
		ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest().withClientId(cognitoClient.clientId)
				.withUsername(email).withSecretHash(secretHash);

		// Initiate the forgot password flow
		return client.forgotPassword(forgotPasswordRequest);

	}

	public ConfirmForgotPasswordResult forgotPasswordConfirm(String email, String newPassword,
			String confirmationCode) {
		// TODO Auto-generated method stub
		String secretHash = calculateSecretHash(email, cognitoClient.clientId, clientSecret);

		// Create the confirm forgot password request
		ConfirmForgotPasswordRequest confirmForgotPasswordRequest = new ConfirmForgotPasswordRequest()
				.withClientId(cognitoClient.clientId).withUsername(email).withPassword(newPassword)
				.withConfirmationCode(confirmationCode).withSecretHash(secretHash);

		// Confirm the forgot password request
		ConfirmForgotPasswordResult confirmForgotPasswordResult = client
				.confirmForgotPassword(confirmForgotPasswordRequest);

		return confirmForgotPasswordResult;
	}

	public ChangePasswordResult changePassword(String prevPassword, String newPassword, String accessToken) {

		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest().withPreviousPassword(prevPassword)
				.withProposedPassword(newPassword).withAccessToken(accessToken);

		// Change the user's password
		return client.changePassword(changePasswordRequest);
	}

//		   System.out.println("jwtClaimsSet="+jwtClaimsSet.toJSONObject()+"\n");
//		   System.out.println("header="+getHeader(idToken)+"\n");
//		   System.out.println("payload="+getPayload(idToken)+"\n");
//		   System.out.println("signature="+getSignature(idToken));

	public Map<String, Object> getUserDetails(String accessToken) {

		GetUserRequest request = new GetUserRequest().withAccessToken(accessToken);

		GetUserResult result = client.getUser(request);

		Map<String, Object> extras = new HashMap<>();

		extras.put("email", result.getUsername());

		for (AttributeType attr : result.getUserAttributes()) {
			extras.put(attr.getName(), attr.getValue());
		}

		return extras;

	}

	public List<UserType> listAllUsers() {
		String userPoolId = "us-east-1_y9fF7Pj4x";
		List<UserType> allUsers = new ArrayList<>();
		String paginationToken = null;

		do {
			ListUsersRequest request = new ListUsersRequest().withUserPoolId(userPoolId).withLimit(60);

			if (paginationToken != null) {
				request.setPaginationToken(paginationToken);
			}

			ListUsersResult result = client.listUsers(request);
			allUsers.addAll(result.getUsers());

			paginationToken = result.getPaginationToken();
		} while (paginationToken != null);

		return allUsers;
	}

	public List<UserResponse> listAllUsersMapped() {
		String userPoolId = "us-east-1_y9fF7Pj4x";
		List<UserType> allUsers = new ArrayList<>();
		String paginationToken = null;

		do {
			ListUsersRequest request = new ListUsersRequest().withUserPoolId(userPoolId).withLimit(60);

			if (paginationToken != null) {
				request.setPaginationToken(paginationToken);
			}

			ListUsersResult result = client.listUsers(request);
			allUsers.addAll(result.getUsers());

			paginationToken = result.getPaginationToken();
		} while (paginationToken != null);

		// Map to UserResponse list
		List<UserResponse> responseList = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();

		for (UserType user : allUsers) {
			UserResponse ur = new UserResponse();
			for (AttributeType attr : user.getAttributes()) {
				switch (attr.getName()) {
				case "sub":
					ur.setUserId(attr.getValue());
					break;
				case "name":
					ur.setName(attr.getValue());
					break;
				case "email":
					ur.setEmail(attr.getValue());
					break;
				case "custom:orgId":
					ur.setOrgId(attr.getValue());
					break;
				case "custom:connectedApp":
					try {
						// The connectedApp attribute is a JSON array string, e.g.
						// [{"app":"FLEET","role":"admin"}]
						// Parse first element to ConnectedApp object
						RoleDto[] apps = objectMapper.readValue(attr.getValue(), RoleDto[].class);
						if (apps.length > 0) {
							ur.setConnectedApp(apps[0]);
						}
					} catch (Exception e) {
						e.printStackTrace();
						ur.setConnectedApp(null);
					}
					break;
				}
			}
			// Password is not accessible; you can set null or empty string
//	        ur.setPassword(null);

			responseList.add(ur);
		}

		return responseList;
	}

	public Map<String, Object> refreshTokens(String username, String refreshToken) {
		Map<String, String> authParams = new HashMap<>();
		authParams.put("REFRESH_TOKEN", refreshToken);

		String secretHash = calculateSecretHash(username);
		if (secretHash != null)
			authParams.put("SECRET_HASH", secretHash);

		AdminInitiateAuthRequest refreshRequest = new AdminInitiateAuthRequest()
				.withUserPoolId(cognitoClient.getUserPool()).withClientId(cognitoClient.getClientId())
				.withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH).withAuthParameters(authParams);
		System.out.println("cognitoClient" + cognitoClient.getUserPool());
		AdminInitiateAuthResult refreshResult = cognitoClient.client.adminInitiateAuth(refreshRequest);
		AuthenticationResultType result = refreshResult.getAuthenticationResult();

		Map<String, Object> tokens = new HashMap<>();
		tokens.put("idToken", result.getIdToken());
		tokens.put("accessToken", result.getAccessToken());
		return tokens;
	}

	private String calculateSecretHash(String username) {
	    if (clientSecret == null || clientSecret.isEmpty()) return null;

	    try {
	        String message = username + cognitoClient.getClientId();
	        SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	        Mac mac = Mac.getInstance("HmacSHA256");
	        mac.init(signingKey);
	        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
	        String secretHash = Base64.getEncoder().encodeToString(rawHmac);

	        // 🔍 Debugging line — print what’s being hashed and the final hash
	        System.out.println("DEBUG: message = " + message);
	        System.out.println("DEBUG: clientSecret = " + clientSecret);
	        System.out.println("DEBUG: generated SECRET_HASH = " + secretHash);

	        return secretHash;
	    } catch (Exception e) {
	        throw new RuntimeException("Error while calculating secret hash", e);
	    }
	}


}
