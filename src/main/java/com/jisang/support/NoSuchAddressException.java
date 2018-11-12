package com.jisang.support;

import com.jisang.config.code.CodeBook.MallLocation;

public class NoSuchAddressException extends RuntimeException {

    private static final long serialVersionUID = -2447205522457173989L;

    private String address;
    private String location;

    public NoSuchAddressException(String message, String address, String location) {
        super(message);
        this.address = address;
        this.location = MallLocation.fromString(location).name();
    }

    public NoSuchAddressException(String message, Throwable e, String address, String location) {
        super(message);
        initCause(e);
        this.address = address;
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }
}
