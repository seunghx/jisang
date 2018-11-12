package com.jisang.security.exception;

import org.springframework.security.core.AuthenticationException;

public class SecurityBadRequestException extends AuthenticationException {

    // StaticFields 
    // ==========================================================================================================================

    private static final long serialVersionUID = -5379429453468846153L;

    // Constructors
    // ==========================================================================================================================

    public SecurityBadRequestException(String msg) {
        super(msg);
    }

    public SecurityBadRequestException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
