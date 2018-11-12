package com.jisang.security.authentication.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**/
/**
 * 
 * 권한이 필요한 자원이나 메서드에 인증되지 않은 사용자가 접근하였을 때 이 클래스의 {@link #commence} 메서드가 호출된다.
 * SecurityExceptionHandlerManager에 전달받은 인자 {@code authException}에 대한 처리를 맡기고
 * 로그인이 필요함을 알리기 위해 응답 처리를 한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationFailureHandler authFailureHandler;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.info("Non authenticated user accessed to authority required resource.");
        logger.info("Delegating exception handling to {}.", authException);

        authFailureHandler.onAuthenticationFailure(request, response, authException);
    }
}
