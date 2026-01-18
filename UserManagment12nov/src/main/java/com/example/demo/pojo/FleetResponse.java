package com.example.demo.pojo;

public class FleetResponse {

    private boolean success;
    private String message;
    private String data;

    public FleetResponse(){}


    public FleetResponse(boolean success, String message, String data ) {
		// TODO Auto-generated constructor stub
    	this.success =success;
    	this.message =message;
    	this.data =data;
	}




	public String getData() {
		return data;
	}




	public void setData(String data) {
		this.data = data;
	}




	public boolean getSuccess() {
		return success;
	}



	public void setSuccess(boolean success) {
		this.success = success;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}





}
