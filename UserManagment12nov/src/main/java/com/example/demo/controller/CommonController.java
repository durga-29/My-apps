package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.Request;
import com.example.demo.pojo.Response;
import com.example.demo.service.CommonService;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/cm")
public class CommonController {

	@Autowired
	CommonService commonService;


	@PostMapping("/signout")
	public Response signOut(@Validated(Request.SignOutValidation.class) @RequestBody Request request) {
		 try {
	            commonService.signOut(request.getRefreshToken());
	            return new Response(true, "signout successful", null);
	        } catch (Exception e) {
	    		Map<String,Object> extraMap =new HashMap<>();
	            extraMap.put("errorMessage", e.getMessage());
	            return new Response(false, "signout failed", extraMap);
	        }

	}

}
