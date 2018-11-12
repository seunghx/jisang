package com.jisang.security.exception.handler;

import java.util.Locale;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * 
 * seucirty 단에서 발생한 {@link ClassCastException}와 같은
 * {@link AuthenticationServiceException} 또는
 * {@link AuthorizationServiceException} 으로 변환된 후에 처리 될 {@link RuntimeException}
 * 타입 예외는 HTTP status 500 - Internal Server Error 로 응답해야 한다. 이런 종류의 예외를 처리하는
 * {@link SecurityExceptionHandler} 구현을 위해 작성된 추상 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public abstract class InternalServerErrorExceptionHandler<T extends RuntimeException>
        extends AbstractSecurityExceptionHandler<T> {

    // Static Fields
    // ==========================================================================================================================

    private static final int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

    // Constructors
    // ==========================================================================================================================

    public InternalServerErrorExceptionHandler(Class<? extends T> representingException, MessageSource msgSource) {
        super(representingException, msgSource);
        validate(representingException);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 이 클래스의 생성자로 전달 되어
     * {@code AbstractSecurityExceptionHandler.representingException} 필드에 담길 인자에 대한
     * 유효성 검사를 한다. 이 추상 클래스를 계승하는 클래스는 {@link AuthenticationServiceException},
     * {@link AuthorizationServiceException}의 하위 타입 예외만을 처리 가능하므로 해당 타입 예외인지를 검사한다.
     * 
     * @throws NullPointerException
     *             인자 {@code e == null}의 경우 발생.
     * @throws IllegalArgumentException
     *             인자가 {@link AuthenticationServiceException},
     *             {@linke AuthorizationServiceException} 타입 클래스의 {@link Class}
     *             오브젝트가 아닐 경우 발생.
     */
    private void validate(Class<? extends T> e) {
        Objects.requireNonNull(e);

        logger.debug("Starting representing exceptionargument validation...");

        if (!(AuthenticationServiceException.class.isAssignableFrom(e)
                                            || AuthorizationServiceException.class.isAssignableFrom(e))) {
            logger.warn("Invalid argument e detected.");
            logger.warn(
                    "e must be subtype of AuthenticationServiceException or AuthorizationServiceExceptoin. but e : {}"
                  , e.toString());
            throw new IllegalArgumentException("Invalid exception type argument detected.");
        }
    }

    protected final int getStatus() {
        return status;
    }

    protected final String getMessage(Locale locale) {
        return msgSource.getMessage("http.response.InternalServerError", null, "An server error occurred", locale);
    }

}
