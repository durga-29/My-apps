package com.example.demo.pojo;


import java.util.Map;


public class Response {

    private boolean success;
    private String message;
    private Map<String,Object> extras;

    public Response(){}


    public Response(boolean success, String message, Map<String,Object> extras ) {
		// TODO Auto-generated constructor stub
    	this.success =success;
    	this.message =message;
    	this.extras =extras;
	}




	public Map<String,Object> getExtras() {
		return extras;
	}




	public void setExtras(Map<String,Object> extras) {
		this.extras = extras;
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
