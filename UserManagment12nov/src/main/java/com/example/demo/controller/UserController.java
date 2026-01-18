package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.Request;
import com.example.demo.pojo.Response;
import com.example.demo.service.UserService;
import com.example.demo.service.ValidationService;
import com.example.dto.RefreshRequest;
import com.example.dto.UserResponse;
import com.example.jwtvalidate.CustomException;

//@CrossOrigin(origins="http://localhost:8088")

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/um")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	ValidationService validationService;

	@GetMapping("/hi")
	public String getMsg() {
		return "Welcome ...";
	}

	@PostMapping("/signup")
	public Response signup(@Validated(Request.SignUpValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			return userService.signUp(request.getEmail(), request.getPassword(), request.getName(),
					request.getConnectedApp(), request.getOrgId());
		} catch (Exception e) {
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Signup failed", extraMap);
		}

	}

	@PostMapping("/confirmSignup")
	public Response confirmSignup(@Validated(Request.SignUpConfirmValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			userService.confirmSignUp(request.getEmail(), request.getConfirmationCode());
			return new Response(true, "Confirmation successful", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Confirmation failed", extraMap);
		}
	}

	@PostMapping("/signIn")
	public Response signIn(@Validated(Request.SignInValidation.class) @RequestBody Request request) {

		return userService.signIn(request.getEmail(), request.getPassword(), request.getConnectedApp());

	}

	@PostMapping("/resetPassword")
	public Response resetPassword(@Validated(Request.AdminResetPasswordValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			userService.resetUserPassword(request.getEmail(), request.getPassword());
			return new Response(true, "reset password  success", null);
		} catch (Exception e) {
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "reset password failed", extraMap);
		}
	}

	@PostMapping("/forgotPassword")
	public Response forgotPassword(@Validated(Request.ForgetPasswordValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			userService.forgotPassword(request.getEmail());
			return new Response(true, "Forgot password request successful", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Forgot password request failed", extraMap);
		}
	}

	@PostMapping("/forgotPasswordConfirmed")
	public Response forgotPasswordConfirm(
			@Validated(Request.ForgetPasswordConfirmValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			userService.forgotPasswordConfirm(request.getEmail(), request.getNewPassword(),
					request.getConfirmationCode());
			return new Response(true, "Forgot password confirmation successful", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Forgot password confirmation failed", extraMap);
		}
	}

	@PostMapping("/changePassword")
	public Response changePassword(@Validated(Request.ChangePasswordValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			userService.changePassword(request.getOldPassword(), request.getNewPassword(), request.getAccessToken());
			return new Response(true, "Change password successful", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Change password failed", extraMap);
		}
	}

	@GetMapping("/getUserDetails")
	public Response getUserDetails(@Validated(Request.GetUserDetailsValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			extraMap = userService.getUserDetails(request.getAccessToken());
			return new Response(true, "User details retrived successfully", extraMap);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Failed", extraMap);
		}
	}

	@PostMapping("/validate")
	public Response validate(@Validated(Request.TokenValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {

			return validationService.validate(request.getEmail(), request.getAccessToken(), request.getRefreshToken());

		} catch (CustomException e) {
			extraMap.put("errorType", e.getErrorMessageType());
			extraMap.put("errorMessage", e.getErrorMessage());
			return new Response(false, "Token validation failed", extraMap);
		}
	}

	@PostMapping("/validateAndRefresh")
	public Response validateAndRefresh(@Validated(Request.TokenValidationRefresh.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {

			return validationService.validate(request.getEmail(), request.getAccessToken(), request.getRefreshToken());

		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Token validation failed", extraMap);
		}
	}

	@GetMapping("/decode/{idToken}")
	public Response decodeToken(@PathVariable String idToken) {
		Map<String, Object> extraMap = new HashMap<>();
		try {

			extraMap = validationService.decodeToken(idToken);
			return new Response(true, "User details retrieved", extraMap);

		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Token validation failed", extraMap);
		}
	}

	@GetMapping("/getAllUsers")
	public List<UserResponse> getUsers() {
		return userService.listAllUsersMapped();
	}

	@GetMapping("/refreshToken")
    public Response refreshTokens(@RequestBody RefreshRequest refreshRequest) {
    	Map<String, Object> emptyMap = new HashMap<>();

        if (refreshRequest.getRefreshToken() == null || refreshRequest.getRefreshToken().isEmpty()) {
        	return new Response(false,"Refresh token is empty",emptyMap);
        }
        try {
            Map<String, Object> newTokens = userService.refreshTokens(refreshRequest.getUsername(), refreshRequest.getRefreshToken());
			return new Response(true, "Tokens refreshed successfully", newTokens);
        } catch (Exception e) {    
        	e.printStackTrace();
            return new Response(false,"Token refresh failed",emptyMap);
            }
    }
}
