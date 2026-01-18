package com.example.demo.pojo;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;


public class Request {

    @NotBlank(message = "Email is required", groups = {UpdateUserAttributes.class,AdminResetPasswordValidation.class,TokenValidationRefresh.class,TokenValidation.class, DeleteUserValidation.class, AddUserValidation.class, SignInValidation.class, SignUpValidation.class,SignUpConfirmValidation.class,ForgetPasswordValidation.class,ForgetPasswordConfirmValidation.class})
    private String email;

    @NotBlank(message = "Password is required", groups = {AdminResetPasswordValidation.class,SignInValidation.class,SignUpValidation.class})
    private String password;

    @NotBlank(message = "Full Name is required", groups = {UpdateUserAttributes.class,AddUserValidation.class,SignUpValidation.class})
    private String name;


//    @NotBlank(message = "ConnectedApp is required", groups = {AddUserValidation.class})
    private ConnectedAppPojo connectedApp;

//    @NotBlank(message = "Role list is required", groups = {UpdateUserAttributes.class,AddUserValidation.class,SignUpValidation.class})
    private List<ConnectedAppPojo> connectedAppList;

    @NotBlank(message = "OrgId is required", groups = {SignUpValidation.class,AddUserValidation.class})
    private String orgId;

    @NotBlank(message = "Confirmation code is required", groups = {SignUpConfirmValidation.class,ForgetPasswordConfirmValidation.class})
    private String confirmationCode;

    @NotBlank(message = "Old password is required", groups = {ChangePasswordValidation.class})
    private String oldPassword;

    @NotBlank(message = "New password is required", groups = {ChangePasswordValidation.class,ForgetPasswordConfirmValidation.class})
    private String newPassword;


    @NotBlank(message = "Access Token is required", groups = {GetUserDetailsValidation.class, TokenValidation.class, TokenValidationRefresh.class,ChangePasswordValidation.class})
    private String accessToken;

    @NotBlank(message = "Refresh Token is required", groups = {TokenValidationRefresh.class, SignOutValidation.class})
    private String refreshToken;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public ConnectedAppPojo getConnectedApp() {
		return connectedApp;
	}

	public void setConnectedApp(ConnectedAppPojo connectedApp) {
		this.connectedApp = connectedApp;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}







	public List<ConnectedAppPojo> getConnectedAppList() {
		return connectedAppList;
	}

	public void setConnectedAppList(List<ConnectedAppPojo> connectedAppList) {
		this.connectedAppList = connectedAppList;
	}







	// Validation groups for user service
	public interface SignInValidation {}
    public interface SignUpValidation {}
    public interface SignUpConfirmValidation {}
    public interface ForgetPasswordValidation {}
    public interface AdminResetPasswordValidation {}
    public interface ForgetPasswordConfirmValidation {}
    public interface ChangePasswordValidation {}

// Validation groups for admin service
    public interface AddUserValidation{}
    public interface DeleteUserValidation{}

//Common Validation groups
    public interface TokenValidation{}
    public interface SignOutValidation{}
    public interface TokenValidationRefresh{}
    public interface GetUserDetailsValidation{}

    public interface UpdateUserAttributes{}



}
