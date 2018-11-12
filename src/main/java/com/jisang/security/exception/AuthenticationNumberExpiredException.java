package com.jisang.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 
 * 사용자의 핸드폰 등으로 전달된 인증 번호의 입력 기한이 만료될 경우 해당 예외가 발생한다.
 * {@link AuthenticationNumberJWTService}에서 {@link JwtExpiredException} 이 발생하면 이
 * 예외로 변환된다. 변환하는 이유는 기본 유저 인증 작업에서의 {@link JwtExpiredException}은 로그인이 필요함을 알리는
 * 다른 예외로 변환되게끔 모든 {@link AbstractJWTService} 구현에 적용되는 애스팩트에 지정되어 있기 때문이다. (처음
 * JWTManager와 이에 대한 애스팩트를 정의할 때는 핸드폰 번호 인증에 대한 구현을 고려하지 않았다. 그러나 크게 나쁘진 않은 것
 * 같다.)
 * 
 * 
 * @author leeseunghyun
 *
 */
public class AuthenticationNumberExpiredException extends AuthenticationException {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = -6517124792432679902L;

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public AuthenticationNumberExpiredException(String message) {
        super(message);
    }

    public AuthenticationNumberExpiredException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
