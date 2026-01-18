package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.Request;
import com.example.demo.pojo.Response;
import com.example.demo.service.AdminService;
import com.example.dto.UserDto;
import com.google.gson.Gson;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/am")
public class AdminController {

	@Autowired
	Gson gson;

	@Autowired
	private AdminService adminService;

	@PostMapping("/")
	public String auth() {
		return "Welcome admin...";
	}

	// @PreAuthorize("@jwtTokenUtil.decryptAndPrintPayload(#token) == 'FEV123' ")
//	/@RequestHeader("Authorization") String token
	@GetMapping("/getAllUser")
	public Response getAllUser() {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			extraMap = adminService.getAllUser();
			return new Response(true, "User retrived successfully", extraMap);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(false, "Failed", extraMap);
		}
	}

	@GetMapping("/getUserByEmail/{email}")
	public Response getUserByEmail(@PathVariable String email) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			UserDto userDto = adminService.convertToUserDto(adminService.getUserDetailByMail(email).getExtras());

			extraMap.put("user", userDto);
			return new Response(true, "User retrived successfully", extraMap);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(false, "Failed", extraMap);
		}
	}

	@PostMapping("/user/{REQUEST_APP_TYPE}")
	public Response signup(@Validated(Request.AddUserValidation.class) @RequestBody Request request,
			@PathVariable("REQUEST_APP_TYPE") String requestAppType) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			adminService.addUser(request, requestAppType);
			return new Response(true, "User added successfully", null);
		} catch (Exception e) {
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Failed", extraMap);
		}
	}

	@PostMapping("/signInWithTempPass")
	public Response signInWithTempPass(@Validated(Request.SignInValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			String challengeName = adminService.initiateAuthWithTemporaryPassword(request.getEmail(),
					request.getPassword());
			extraMap.put("account_status", "FORCE_CHANGE_PASSWORD");
			return new Response(true, "Temporary password is verified", extraMap);
		} catch (Exception e) {
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Failed", extraMap);
		}
	}

	@DeleteMapping("/user")
	public Response deleteUser(@Validated(Request.DeleteUserValidation.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			adminService.deleteUser(request.getEmail());
			return new Response(true, "User deleted successfully", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Failed", extraMap);
		}
	}

	@PutMapping("/user")
	public Response updateUser(@Validated(Request.UpdateUserAttributes.class) @RequestBody Request request) {
		Map<String, Object> extraMap = new HashMap<>();
		try {
			Gson gson = new Gson();
			adminService.updateCustomAttribute(request.getEmail(), request.getName(), "custom:connectedApps",
					gson.toJson(request.getConnectedAppList()));
			return new Response(true, "User updated successfully", null);
		} catch (Exception e) {
			extraMap.put("errorCode", "" + "404");
			extraMap.put("errorMessage", e.getMessage());
			return new Response(false, "Failed", extraMap);
		}
	}

}
