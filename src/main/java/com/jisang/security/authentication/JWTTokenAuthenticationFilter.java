package com.jisang.security.authentication;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import com.jisang.security.core.DefaultUserDetailsAuthentication;
import com.jisang.security.core.UserAuthTokenDTOAuthentication;
import com.jisang.security.core.userdetails.DefaultUserDetails;
import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.exception.IllegalAuthenticationProviderUsedException;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;
import com.jisang.security.validation.JWTBasedUserAuthentication;
import com.jisang.security.validation.SecurityValidationDelegator;

/**/
/**
 * 
 * 로그인 상태의 유지를 위해 사용되는 인증 필터이다. HTTP 요청 헤더로부터 JWT token 문자열을 받고
 * {@link AbstractJWTService}, {@link AuthenticationProvider} 에 JWT token 검사를
 * 맡긴다. {@code io.jsonwebtoken} 라이브러리를 이용한 정적으로 고정된 JWT token 검사는
 * {@link AbstractJWTService#validateToken} 을 호출해 수행하며 특정 조건하에 수행되어야 할 JWT token
 * 검사는 {@link AuthenticationProvider#authenticate(Authentication)} 메서드가 진행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class JWTTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JWTServiceResolver jwtServiceResolver;
    @Autowired
    private SecurityValidationDelegator validator;

    // Constructors
    // ==========================================================================================================================

    public JWTTokenAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * JWT token에 대한 인증을 수행한다. 인자로 전달 받은 {@code request}의 헤더로 부터 추출한 JWT token 문자열을
     * 담은 {@link UserAuthTokenDTO} 오브젝트를 {@code jwtManager#parseToken}에 전달하는 것을 알 수
     * 있는데 이 메서드는 내부적으로 토큰에 대한 유효성 검사까지 수행한다. 유효성 검사에 통과한 경우에만 어플리케이션의 실제적인 인증을 처리하는
     * {@link AuthenticationProvider} 구현에 인증을 위임한다. {@link jwtManager#parseToken}
     * 메서드를 이용한 토큰의 유효성 검사와 {@link AuthenticationProvider} 구현의 인증 절차가 분리되어 있는데, JWT
     * token의 유효성 검사 또한 인증의 한 부분으로 볼 수 있기 때문에 {@link AuthenticationProvider}에서
     * {@link AbstractJWTService}를 이용하게 할 수도 있었으나 {@link AuthenticationProvider}에서는
     * 어플리케이션의 인증 정책에 따르는 로직이 필요한 인증만을 수행하도록 하였다.
     * 
     * 예를 들어 이 어플리케이션의 인증 정책하에서는 JWT token을 구성하는 정보 중 토큰의 id를 의미하는 JTI 필드를 비교할 때, 매번
     * 데이터베이스로부터 해당 유저에 대하여 저장되어 있던 JTI 칼럼을 조회함으로서 발생하는 오버헤드를 피하기 위해 한 차례 파싱 된 JWT
     * token 정보로 부터 토큰 갱신 시점을 확인하여 토큰 갱신 시점이 지난 JWT token에 대해서만 JTI비교를 수행한다. if문 등을
     * 이용한 분기가 필요한 인증 작업이기 때문에 이런 검증 작업은 {@link AuthenticationProvider}가 수행하도록 하였고
     * 이런 로직이 담긴 검증이 불가능한 {@link io.jsonwebtoken} 라이브러리를 이용하는
     * {@link AbstractJWTService}에서는 보다 정적인 검증(유효성 검사)만을 수행하게 하였다.
     * 
     * 이런 부분은 구현 나름이기 때문에 {@link AuthenticationProvider}에게 유효성 검사와 인증 절차 등을 모두 맡길 수도
     * 있다. 만약 토큰에 대한 유효성 검사와 검사에 통과한 토큰에 대한 인증 절차까지 수행한다면
     * {@link AuthenticationProvider} 구현이 매우 복잡해질 것이다.
     * 
     * 
     * 
     * @return 인증이 성공할 경우 {@link AuthenticationProvider}에서 반환한
     *         {@link Authentication} 오브젝트를 반환한다.
     * @throws AuthenticationException
     *             {@link AbstractJWTService#validateToken(String)}
     * 
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        logger.info("Attempting authentication....");

        String tokenHeader = request.getHeader(JWTService.JWT_HEADER_NAME);

        if (StringUtils.isEmpty(tokenHeader)) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is not present");
        }

        if (!tokenHeader.startsWith(JWTService.JWT_PREFIX)) {
            logger.warn("Unknown token received. - this token does not start with {}", JWTService.JWT_PREFIX);
            throw new BadCredentialsException("Received unknown token.");
        }

        JWTService jwtService = jwtServiceResolver.resolveJWTService(UserAuthTokenDTO.class);

        return getAuthenticationManager().authenticate(new UserAuthTokenDTOAuthentication((UserAuthTokenDTO) jwtService
                .parseToken(tokenHeader.substring(JWTService.JWT_PREFIX.length(), tokenHeader.length()))));
    }

    /**
     * 
     * JWT token 인증이 성공되었을 경우에 호출되는 메서드이다.
     * {@link AuthenticationProvider#authenticate} 메서드가 반환한
     * {@code authentication}으로부터 token 정보를 담고 있는 {@link UserAuthTokenDTO}를 받아온다.
     * {@link #attemptAuthentication} 메서드에서 한 차례 JWT token에 대한 parsing이 이루어졌음에도 또 다시
     * token 정보를 받는 이유는 JTI, IAT 등의 claim 정보가 갱신되었을 수 있기 때문이다. 이 클래스에서 발생한 예외는 이
     * 메서드를 호출한 {@link AbstractAuthenticationProcessingFilter#doFilter}에 전달되지 않게
     * 하기위해 {@link #unsuccessfulAuthentication}메서드로 전달된다. 이 후의 {@link Filter}에서 알 수
     * 있도록 {@link SecurityContextHolder}에 인증 정보를 담은
     * {@link DefaultUserDetailsAuthentication}이 담긴 SecurityContext 오브젝트를 전달한다.
     * 
     */
    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException, ServletException {

        logger.info("Authentication success. Passing process to next filter chain...");

        try {
            UserAuthTokenDTO tokenDTO = Optional.ofNullable(authentication)
                    .filter(auth -> auth instanceof UserAuthTokenDTOAuthentication)
                    .map(auth -> (UserAuthTokenDTO) auth.getPrincipal()).orElseThrow(() -> {
                        logger.info("Argument authentication to be instance of {}. but authentication : {}"
                                                        , UserAuthTokenDTOAuthentication.class, authentication);
                        return new IllegalAuthenticationProviderUsedException(
                                        "Argument authentication is not instance of TokenDTOBasedAuthentication.");
                    });

            validator.validate(tokenDTO, JWTBasedUserAuthentication.class);

            String jwt = jwtServiceResolver.resolveJWTService(tokenDTO.getClass())
                                           .buildToken(tokenDTO);

            DefaultUserDetailsAuthentication userAuth = new DefaultUserDetailsAuthentication(
                                                new DefaultUserDetails(tokenDTO.getAccount()), true);

            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(userAuth);
            SecurityContextHolder.setContext(sc);

            logger.debug("Setting jwt token to response object...");

            response.addHeader(JWTService.JWT_HEADER_NAME, JWTService.JWT_PREFIX + jwt);

            chain.doFilter(request, response);

        } catch (InternalAuthenticationServiceException e) {
            logger.info("InternalAuthenticationServiceException catched at successfulAuthentication().");
            unsuccessfulAuthentication(request, response, e);
        }
    }

    /**
     * 
     * 인증이 실패하였을 경우 전달 된 {@code exception}의 처리와 HTTP 응답 처리를 수행한다. 실제 예외 처리는
     * {@link SecurityExceptionHandlerManager}에 맡기고 HTTP 응답 처리는
     * {@link SecurityRestResponseHelper} 오브젝트에게 맡긴다.
     * 
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (logger.isInfoEnabled()) {
            logger.info("Authentication request failed: {}", exception.getMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Delegating to AuthenticationFailureHandler...");
        }
        getFailureHandler().onAuthenticationFailure(request, response, exception);
    }

}
