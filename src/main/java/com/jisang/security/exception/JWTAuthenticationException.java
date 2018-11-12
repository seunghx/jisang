package com.jisang.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JWTAuthenticationException extends AuthenticationException {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = -1284357512993258305L;

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public JWTAuthenticationException(String message) {
        super(message);
    }

    public JWTAuthenticationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
