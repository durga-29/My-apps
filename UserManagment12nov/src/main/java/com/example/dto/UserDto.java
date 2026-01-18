package com.example.dto;

import java.util.List;


public class UserDto {

	private String name;
	private String email;
	private List<RoleDto> roles;

	public UserDto(String name, String email, List<RoleDto> roles) {
		super();
		this.name = name;
		this.email = email;
		this.roles = roles;
	}


	public UserDto() {
		// TODO Auto-generated constructor stub
	}


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<RoleDto> getRoles() {
		return roles;
	}
	public void setRoles(List<RoleDto> roles) {
		this.roles = roles;
	}


	@Override
	public String toString() {
		return "UserDto [name=" + name + ", email=" + email + ", roles=" + roles + "]";
	}




}
