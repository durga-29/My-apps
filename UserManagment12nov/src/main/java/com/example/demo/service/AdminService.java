package com.example.demo.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.example.demo.pojo.ConnectedAppPojo;
import com.example.demo.pojo.Request;
import com.example.demo.pojo.Response;
import com.example.dto.RoleDto;
import com.example.dto.UserDto;
import com.example.jwtvalidate.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@Service
public class AdminService {

	private  AWSCognitoIdentityProvider client;
	private  CognitoClient cognitoClient;


	Gson gson = new Gson();


	@Autowired
	private FleetService fleetService;


	@Autowired
	public AdminService(CognitoClient cognitoClient) {
		this.cognitoClient =cognitoClient;
		client =cognitoClient.client;
	}


	public Map<String,Object> getAllUser() throws JsonProcessingException {
		// TODO Auto-generated method stub
		Map<String,Object> extraMap =new HashMap<>();
		List<UserDto> userList =new ArrayList<>();
		Gson gson = new Gson();


	        ListUsersRequest request = new ListUsersRequest()
	                .withUserPoolId(cognitoClient.userPool)
	             //   .withFilter("role != \"admin\"")
	                ;

	        ListUsersResult result = client.listUsers(request);
	        ObjectMapper objectMapper = new ObjectMapper();

	        int i=1;
	        for (UserType userType : result.getUsers()) {

	        	Map<String,Object> tempMap =new HashMap<>();


	            tempMap.put("email",userType.getUsername());
	            tempMap.put("userStatus",userType.getUserStatus());
	            tempMap.put("isActive",""+userType.getEnabled());
	            tempMap.put("UserCreatedDate",""+userType.getUserCreateDate());


	         // Fetch custom attribute
	            for (AttributeType attribute : userType.getAttributes()) {
		            tempMap.put(attribute.getName(),attribute.getValue());

	            }



	        	// Define the type of the target object
				Type type = new TypeToken<ArrayList<RoleDto>>() {
				}.getType();

	            ArrayList<RoleDto> roleList  = gson.fromJson(""+tempMap.get("custom:connectedApps"),type);


	            UserDto userDto =new UserDto(
	        			 ""+tempMap.get("name"),
	        			 ""+tempMap.get("email"),
	        			 roleList);

	            userList.add(userDto);

		        System.out.println(userDto.toString());

//	            String jsonString = objectMapper.writeValueAsString(tempMap);
//	            extraMap.put("User"+(i++),jsonString);


	        }

	        extraMap.put("users", userList);


	        return extraMap;

	}

	public void addUser(Request request, String requestAppType) throws JsonProcessingException {
		// TODO Auto-generated method stub

		List<ConnectedAppPojo> connectedAppList = new ArrayList<>();
		connectedAppList.add(request.getConnectedApp());


		String[] split =request.getEmail().split("@");
		System.out.println(request.getEmail());
		System.out.println("request"+request.getName());
		System.out.println("split[0] "+split[0]);
		AdminCreateUserRequest awsRequest = new AdminCreateUserRequest()
                .withUserPoolId(cognitoClient.userPool)
//                .withUsername(split[0])
                .withUsername(request.getEmail())
                .withUserAttributes(
                        new AttributeType().withName("custom:connectedApps").withValue(gson.toJson(connectedAppList)),
                        new AttributeType().withName("email").withValue(request.getEmail()),
                        new AttributeType().withName("custom:orgId").withValue(request.getOrgId()),
                        new AttributeType().withName("name").withValue(request.getName()),
                        new AttributeType().withName("email_verified").withValue("true"))
                        // Add other user attributes if needed
                .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL);

        AdminCreateUserResult result = client.adminCreateUser(awsRequest);

//        addUserToRequestAppDb(request,requestAppType);

        }


	public void addUserToRequestAppDb(Request request,String requestAppType) throws JsonProcessingException{

		switch(requestAppType) {

		case "FLEET":
			fleetService.postDataToAPI(request);
			break;
		}


		//Calling add user api in FleetManagment
        //
	}



	 public String initiateAuthWithTemporaryPassword(String email, String temporaryPassword) throws CustomException {

		 Map<String, String> authParameters = new HashMap<>();
	        authParameters.put("USERNAME", email);
	        authParameters.put("PASSWORD", temporaryPassword);


		 AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest()
	                .withUserPoolId(cognitoClient.userPool)
	                .withClientId(cognitoClient.clientId)
	                .withAuthFlow("ADMIN_NO_SRP_AUTH")
	                .withAuthParameters(authParameters);
	        try {
	            AdminInitiateAuthResult initiateAuthResult = client.adminInitiateAuth(initiateAuthRequest);
	            // The result will indicate that a password change is required.
	            return initiateAuthResult.getChallengeName();
	        } catch (Exception e) {
	        	throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Authentication error" , e.getMessage());
	        }
	    }



	public void deleteUser(String email) {
		// TODO Auto-generated method stub
		AdminDeleteUserRequest request = new AdminDeleteUserRequest()
                .withUserPoolId(cognitoClient.userPool)
                .withUsername(email);

        client.adminDeleteUser(request);

	}



		public Response resetUserPassword(String username, String newPassword) {

			Map<String,Object> extraMap =new HashMap<>();

			AdminSetUserPasswordRequest setPasswordRequest = new AdminSetUserPasswordRequest()
		                .withUserPoolId(cognitoClient.userPool)
		                .withUsername(username)
		                .withPassword(newPassword)
		                .withPermanent(true); // Set to false to clear "FORCE_CHANGE_PASSWORD" status

		        try {
		            AdminSetUserPasswordResult setPasswordResponse = client.adminSetUserPassword(setPasswordRequest);
		            // Handle success
				    return new Response(true,"Account confirmed ",null);
		        } catch (Exception e) {
		            // Handle error
		        	extraMap.put("errorMessage", e.getMessage());
				    return new Response(false,"Account confirmed failed",extraMap);

		        }
    }



	public Response getUserDetailByMail(String email){

		Map<String,Object> extraMap =new HashMap<>();

		 AdminGetUserRequest request = new AdminGetUserRequest()
		            .withUserPoolId(cognitoClient.userPool)
		            .withUsername(email);

	        try {

			    AdminGetUserResult result = client.adminGetUser(request);

			    extraMap.put("account_status",  result.getUserStatus());


			    for(AttributeType a : result.getUserAttributes()) {
			    	extraMap.put(a.getName(), a.getValue());
			    }

			    return new Response(true,"Account found",extraMap);

	        }catch (Exception e) {

	        	extraMap.put("errorMessage", e.getMessage());
	        	return new Response(false,"retriving user detail failed",extraMap);

	        }

	}


public UserDto convertToUserDto(Map<String,Object> extraMap){

	 Type type = new TypeToken<ArrayList<RoleDto>>() {
		}.getType();

     ArrayList<RoleDto> roleList  = gson.fromJson(""+extraMap.get("custom:connectedApps"),type);

     UserDto userDto =new UserDto(
			 ""+extraMap.get("name"),
			 ""+extraMap.get("email"),
			 roleList);


     return userDto;
	}




	 public Response updateCustomAttribute(String email,String name,String attrName, String attrVal) {

			Response response =new Response();

			 AttributeType nameAttribute = new AttributeType()
		                .withName("name")
		                .withValue(name);


	    	 // Create the attribute to update
         AttributeType attribute = new AttributeType()
                 .withName(attrName)
                 .withValue(attrVal);

         // Create the request to update the user attribute
         AdminUpdateUserAttributesRequest request = new AdminUpdateUserAttributesRequest()
                 .withUserPoolId(cognitoClient.userPool)
                 .withUsername(email)
                 .withUserAttributes(nameAttribute,attribute);

         try {
         // Call the API to update the user attribute
         AdminUpdateUserAttributesResult result = client.adminUpdateUserAttributes(request);
         response.setSuccess(true);
     	response.setMessage("Updating user attribute success");

         }catch (AWSCognitoIdentityProviderException e) {
         	response.setSuccess(false);
         	response.setMessage("updateCustomAttribute() error : Error updating user attribute:");
             System.out.println("Error updating user attribute: " + e.getMessage());

         }catch (Exception e) {
         	response.setSuccess(false);
         	response.setMessage("updateCustomAttribute() error : "+e.getMessage());
             System.out.println("Error updating user attribute: " + e.getMessage());

         }


         return response;
		}




}
