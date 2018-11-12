package com.jisang.security.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.jisang.security.core.AnonymousUserAuthentication;
import com.jisang.security.core.AuthNumberTokenDTOAuthentication;
import com.jisang.security.dto.AnonymousUserAuthTokenDTO;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;

/**
 * 
 * (비밀 번호를 기억못하는)익명 사용자의 임시 비밀번호 발급 요청에 대한 인증을 수행하는
 * {@link AbstractAuthenticationProcessingFilter} 구현(추상 클래스 상속)이다. 익명 사용자에 대한
 * 인증을 위해 이 클래스의 필드 {@code jwtService}에서 내부적으로 사용하는
 * {@link io.jsonwebtoken.JwtParser#parsetClaimsJws()} 메서드는 JWT token의 파싱 작업 중에
 * 토큰의 검증까지 수행한다. 그래서 이 클래스는 따로 {@link AuthenticationProvider} 구현에 인증을 맡기지 않는다.
 * 인증에 성공할 경우 임시 비밀번호를 생성하여 사용자에게 전달하며 (현재는 이메일을 통해 전달.) 해당 사용자의 데이터베이스 상의 비밀번호
 * 정보를 생성된 임시 비밀번호로 변경한다.
 * 
 * @author leeseunghyun
 *
 */
public class TemporaryPasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(TemporaryPasswordAuthenticationFilter.class);

    @Autowired
    private JWTServiceResolver jwtServiceResolver;

    // Constructors
    // ==========================================================================================================================

    public TemporaryPasswordAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 임시 비밀번호 발급 요청에 대한 익명 사용자(로그인 하지 않은 사용자)의 요청을 인증한다. 인증은
     * {@code JWTService#parseToken} 메서드를 통해 진행된다. 위에서 말한대로 이 메서드는 내부적으로
     * {@link io.jsonwebtoken.JwtParser#parseClaimsJws} 메서드를 사용하는데 이 메서드에는 파싱 작업과 함께
     * 토큰의 검증 작업까지 이루어진다.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        logger.info("Attempting authentication....");

        String tokenHeader = request.getHeader(JWTService.JWT_HEADER_NAME);

        if (tokenHeader == null) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is not present in request header.");
        }

        if (!tokenHeader.startsWith(JWTService.JWT_PREFIX)) {
            logger.warn("Unknown token received. - this token does not start with {}", JWTService.JWT_PREFIX);
            throw new BadCredentialsException("Received unknown token.");
        }

        String jwt = tokenHeader.substring(JWTService.JWT_PREFIX.length(), tokenHeader.length());

        AnonymousUserAuthTokenDTO tokenDTO = new AnonymousUserAuthTokenDTO();
        tokenDTO.setToken(jwt);
        tokenDTO.setClientIPAddr(request.getRemoteAddr());

        return new AnonymousUserAuthentication((AnonymousUserAuthTokenDTO) jwtServiceResolver
                .resolveJWTService(tokenDTO.getClass()).parseToken(tokenDTO));

    }

    /**
     * 이 클래스의 인자 {@code authentication}의 {@code getPrincipal} 메서드의 반환값을 바로 형변환해도 안전한
     * 이유는 {@code authentication}은 이 클래스의 메서드 {@link #attemptAuthentication}에서 반환한
     * {@link AuthNumberTokenDTOAuthentication} 오브젝트이기 때문이다.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws ServletException, IOException {

        logger.info("Authentication success. Passing process to next filter chain...");

        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(authentication);
        SecurityContextHolder.setContext(sc);

        chain.doFilter(request, response);

    }

    /**
     * 인증 실패시에 호출되는 메서드이다. 이 클래스의 {@link #unsuccessfulAuthentication} 메서드 구현은
     * {@link AuthenticationFailureHandler#onAuthenticationFailure}를 호출하여 응답 처리를
     * 위임한다.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws ServletException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Authentication request failed: {}", exception.getMessage());
        }

        logger.debug("Delegating to AuthenticationFailureHandler...");

        getFailureHandler().onAuthenticationFailure(request, response, exception);
    }

}
