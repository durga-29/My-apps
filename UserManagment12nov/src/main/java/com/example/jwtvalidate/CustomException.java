package com.example.jwtvalidate;

import org.springframework.http.HttpStatus;

public class CustomException extends Exception {

	 private final HttpStatus httpStatus;
	 private final String errorMessageType;
	 private final String errorMessage;


	    public CustomException(HttpStatus httpStatus, String errorMessageType, String errorMessage) {
	        super(errorMessage);
	        this.httpStatus =httpStatus;
	        this.errorMessageType = errorMessageType;
	        this.errorMessage = errorMessage;
	    }

	    public String getErrorMessageType() {
	        return errorMessageType;
	    }


	    public HttpStatus getHttpStatus() {
	        return httpStatus;
	    }

	    public String getErrorMessage() {
	        return errorMessage;
	    }

}