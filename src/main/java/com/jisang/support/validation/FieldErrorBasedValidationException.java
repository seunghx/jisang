package com.jisang.support.validation;

import java.util.Set;

import javax.validation.ValidationException;

import org.springframework.validation.FieldError;

/**
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class FieldErrorBasedValidationException extends ValidationException {

    private static final long serialVersionUID = -5645167613644318588L;
    private Set<FieldError> errors;

    public FieldErrorBasedValidationException(String message) {
        super(message);
    };

    public FieldErrorBasedValidationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public FieldErrorBasedValidationException(String message, Set<FieldError> errors) {
        this(message);
        this.errors = errors;
    }

    public FieldErrorBasedValidationException(String message, Throwable cause, Set<FieldError> errors) {
        this(message, cause);
        this.errors = errors;
    }

    public Set<FieldError> getFieldExceptions() {
        return errors;
    }

}
