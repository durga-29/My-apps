package com.example.demo.pojo;

import org.hibernate.validator.constraints.NotBlank;

import com.example.demo.pojo.Request.AddUserValidation;
import com.example.demo.pojo.Request.SignInValidation;
import com.example.demo.pojo.Request.SignUpValidation;
import com.example.demo.pojo.Request.UpdateUserAttributes;

public class ConnectedAppPojo {

    @NotBlank(message = "App is required", groups = {UpdateUserAttributes.class,SignInValidation.class,SignUpValidation.class,AddUserValidation.class})
	String app;

    @NotBlank(message = "Role is required", groups = {UpdateUserAttributes.class,SignInValidation.class,SignUpValidation.class,AddUserValidation.class})
    String role;

	public ConnectedAppPojo(String app, String role) {
		super();
		this.app = app;
		this.role = role;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
