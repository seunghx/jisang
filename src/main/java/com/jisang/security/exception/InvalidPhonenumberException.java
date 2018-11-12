package com.jisang.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 
 * 등록되지 않은 핸드폰 번호.
 * 
 * @author leeseunghyun
 *
 */
public class InvalidPhonenumberException extends AuthenticationException {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = 3057079398385700842L;

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public InvalidPhonenumberException(String message) {
        super(message);
    };

    public InvalidPhonenumberException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    // Methods
    // ==========================================================================================================================

}
