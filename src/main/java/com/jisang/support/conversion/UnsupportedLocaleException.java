package com.jisang.support.conversion;

public class UnsupportedLocaleException extends RuntimeException {

	
	// Static Fields
	//==========================================================================================================================
	
	private static final long serialVersionUID = -166077016980146439L;

		
	// Instance Fields
	//==========================================================================================================================

	private String topic;

	// Constructors
	//==========================================================================================================================
	
	public UnsupportedLocaleException(String message) {
		super(message);
	}
		
	public UnsupportedLocaleException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
	
	public UnsupportedLocaleException(String message, String topic) {
		this(message);
		this.topic = topic;
	}
		
	public UnsupportedLocaleException(String message, Throwable cause, String topic) {
		this(message, cause);
		this.topic = topic;
	}
	

	// Method
	//==========================================================================================================================
	
	public String getTopic() {
		return topic;
	}
}
