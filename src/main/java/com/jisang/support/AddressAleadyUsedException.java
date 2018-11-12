package com.jisang.support;

import com.jisang.config.code.CodeBook.MallLocation;

public class AddressAleadyUsedException extends RuntimeException {

	private static final long serialVersionUID = -4036426493856966352L;
	
	private String address;
	private String location;
	
	public AddressAleadyUsedException(String message, String address, String location) {
		super(message);
		this.address = address;
		this.location = MallLocation.fromString(location).name();
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getLocation() {
		return location;
	}
}
