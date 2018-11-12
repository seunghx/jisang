package com.jisang.security.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import com.jisang.security.core.AuthNumberTokenDTOAuthentication;
import com.jisang.security.dto.AnonymousUserAuthTokenDTO;
import com.jisang.security.dto.AuthenticationNumberTokenDTO;
import com.jisang.security.exception.BadRequestParameterDetectedException;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;

/**
 * 
 * 사용자가 전송한 인증 번호가 올바른지를 판단하는 인증 {@link Filter} 구현이다.
 * 
 * 올바른 인증 번호를 전달한 사용자에게 응답을 할 때 또 다른 JWT token의 전송이 필요하다. 인증 번호 검사에 성공한 사용자가 새로운
 * 요청을 할 경우 (예를 들면 "임시 비밀번호 발급"과 같은 버튼 클릭) 해당 요청이 인증된(인증 번호 검사에 성공한) 사용자로부터 전송
 * 되었다는 것을 알려야하기 때문이며 또한 방금 언급한 "임시 비밀번호 발급" 시나리오의 경우 임시 비밀번호를 발급할 사용자의 신원(id)를
 * 알아야 핸드폰이나 이메일 등에 임시 비밀번호를 전송할 수 있기 때문에 사용자의 신원(id)정보가 담긴 JWT token이 꼭 필요하다.
 * 
 * 처음엔 임시 비밀번호 발급만을 위하여 이 클래스를 정의하려 하였다. 그렇기 때문에 초기의 구상에서는 요청이 인증 번호 검사에 성공할 경우
 * 이 클래스에서 바로 사용자의 이메일에 임시 비밀번호를 전송한다. 그러나 후에 인증 번호 검사가 필요한 또 다른 시나리오가 생길 수 있기
 * 때문에 이 클래스에서 임시 비밀번호를 보내버리면 재사용성 측면에서 아쉬움이 있을 것이다.
 * 
 * 위와 같은 이유로 이 클래스는 인증 번호 검사에 성공할 경우 인증 되었음과 유저의 신원(id)를 알리기 위한 또 다른 JWT 토큰을
 * 발급한다.
 *
 * 
 * @author leeseunghyun
 *
 */
public class AuthenticationNumberAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // Static Fields
    // ==========================================================================================================================

    // Request parameters name..
    private static final String AUTHENTICATION_NUMBER_PARAMETER = "authentication-number";

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(AuthenticationNumberAuthenticationFilter.class);

    @Autowired
    private JWTServiceResolver jwtServiceResolver;

    // Constructors
    // ==========================================================================================================================

    public AuthenticationNumberAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 인증 번호에 대한 인증을 시도한다. 이 {@code attemptAuthentication()} 구현은 따로
     * {@link AuthenticationProvider}에 인증을 맡기지 않고 {@link JWTService}의 JWt token
     * parsing 중에 수행되는 검증 방식을 이용해 인증 번호를 검사한다.
     * 
     */
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        logger.info("Attempting authentication....");

        String authenticationNumber = request.getParameter(AUTHENTICATION_NUMBER_PARAMETER);

        if (StringUtils.isEmpty(authenticationNumber)) {
            throw new BadRequestParameterDetectedException("Request parameter authenticationNumber is null.",
                    AUTHENTICATION_NUMBER_PARAMETER, authenticationNumber);
        }

        String tokenHeader = request.getHeader(JWTService.JWT_HEADER_NAME);

        if (tokenHeader == null) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is not present in request header.");
        }

        if (!tokenHeader.startsWith(JWTService.JWT_PREFIX)) {
            logger.warn("Unknown token received. - this token does not start with {}", JWTService.JWT_PREFIX);
            throw new BadCredentialsException("Received unknown token.");
        }

        String jwt = tokenHeader.substring(JWTService.JWT_PREFIX.length(), tokenHeader.length());

        AuthenticationNumberTokenDTO authNumberTokenDTO = new AuthenticationNumberTokenDTO();
        authNumberTokenDTO.setAuthenticationNumber(authenticationNumber);
        authNumberTokenDTO.setClientIPAddr(request.getRemoteAddr());
        authNumberTokenDTO.setToken(jwt);

        JWTService jwtService = jwtServiceResolver.resolveJWTService(authNumberTokenDTO.getClass());
        return new AuthNumberTokenDTOAuthentication(
                (AuthenticationNumberTokenDTO) jwtService.parseToken(authNumberTokenDTO));
    }

    /**
     * 인증 번호 인증에 성공하였을 경우에 호출되는 메서드이다. 인증번호 검사에 성공했음을 알리는 응답을 보낸다. 이 클래스의 인자
     * {@code authentication}의 {@code getPrincipal} 메서드의 반환값을 바로 형변환해도 안전한 이유는
     * {@code authentication}은 이 클래스의 메서드 {@link #attemptAuthentication}에서 반환한
     * {@link AuthNumberTokenDTOAuthentication} 오브젝트이기 때문이다.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws ServletException, IOException {

        logger.info("Authentication success. Processing notification...");

        String userEmail = ((AuthenticationNumberTokenDTO) authentication.getPrincipal()).getUserEmail();

        AnonymousUserAuthTokenDTO tokenDTO = new AnonymousUserAuthTokenDTO();
        tokenDTO.setUserEmail(userEmail);
        tokenDTO.setClientIPAddr(request.getRemoteAddr());

        String jwt = jwtServiceResolver.resolveJWTService(tokenDTO.getClass()).buildToken(tokenDTO);

        response.setStatus(HttpStatus.OK.value());
        response.setHeader(JWTService.JWT_HEADER_NAME, JWTService.JWT_PREFIX + jwt);

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
