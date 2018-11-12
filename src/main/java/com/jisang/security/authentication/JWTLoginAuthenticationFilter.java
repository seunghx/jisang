package com.jisang.security.authentication;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import io.jsonwebtoken.JwtException;

import com.jisang.security.core.DefaultUserDetailsAuthentication;
import com.jisang.security.core.JWTUsernamePasswordAuthentication;
import com.jisang.security.core.UserAuthTokenDTOAuthentication;
import com.jisang.security.domain.Account;
import com.jisang.security.domain.TokenComponent;
import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.exception.BadRequestParameterDetectedException;
import com.jisang.security.exception.IllegalAuthenticationProviderUsedException;
import com.jisang.security.exception.handler.SecurityExceptionHandler;
import com.jisang.security.persistence.RedisUserDAO;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;
import com.jisang.security.validation.SecurityValidationDelegator;
import com.jisang.security.validation.UserAuthentication;

/**/
/**
 * 
 * JWT 기반 인증에서의 로그인 인증을 담당하는 {@link AbstractAuthenticationProcessingFilter} 구현
 * 필터 클래스이다. 일반적으로 진행 될 JWT 기반 인증 과정은 {@link JWTTokenAuthenticationFilter}에서
 * 수행하고 로그인 인증의 경우에만 이 필터 클래스에서 수행한다. 폼 로그인 방식이 진행되기 때문에 폼 로그인 인증 방식을 수행하는
 * {@link AuthenticationProvider}에 인증 절차를 맡긴다.
 * 
 * 
 * @author leeseunghyun
 * 
 */
public class JWTLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // Static Fields
    // ==========================================================================================================================

    // Request parameters name..
    private static final String EMAIL_PARAMETER = "email";
    private static final String PASSWORD_PARAMETER = "password";

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(JWTLoginAuthenticationFilter.class);

    @Autowired
    private JWTServiceResolver jwtServiceResolver;
    @Autowired
    private SecurityValidationDelegator validator;
    @Autowired
    private RedisUserDAO redisUserDAO;

    // Constructors
    // ==========================================================================================================================

    public JWTLoginAuthenticationFilter(RequestMatcher reqMatcher) {
        super(reqMatcher);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 로그인 인증을 시도한다. 이 {@code attemptAuthentication()} 구현은 유저 아이디와 패스워드에 대한 컨테이너 역할을
     * 하는 {@link JWTUsernamePasswordAuthentication} 오브젝트를
     * {@link AuthenticationProvider}에 전달하며 실제 인증 작업을 위임한다. 요청 파라미터에 대하여 null 검사만
     * 진행하는 데, 그 이유는 빈 문자열은 어차피 인증에 실패할 것이기 때문이다.
     * 
     * @throws UsernameNotFoundException
     *             HTTP 요청 파라미터 {@code email}이 전달되지 않았을 경우에 던져진다.
     * @throws BadCredentialsException
     *             HTTP 요청 파라미터 {@code password}가 전달되지 않았을 경우에 던져진다.
     * @throws (UsernameNotFoundException,
     *             BadCredentialsException) : 잘못된 파라미터에 대하여 HTTP 400 응답으로 처리하여도 되겠지만
     *             {@link SecurityExceptionHandler} 구현의 편의를 위하여
     *             {@link SecurityExceptionhandler#handle(RuntimeException)} 메서드의 의해
     *             HTTP 401 응답으로 처리되어지는 UsernameNotFoundException,
     *             BadCredentialsException을 던지도록 하였다.
     * @throws AuthenticationException
     *             이 메서드가 인증 작업을 위임하는 {@link AuthenticationProvider#authenticate()}
     *             메서드에서 발생하는 {@link AuthenticationException} 타입 예외들이 던져진다.
     * 
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        logger.info("Attempting authentication....");

        String userEmail = request.getParameter(EMAIL_PARAMETER);
        String password = request.getParameter(PASSWORD_PARAMETER);

        if (StringUtils.isEmpty(userEmail)) {
            logger.info("Request parameter {} is empty.", EMAIL_PARAMETER);
            throw new BadRequestParameterDetectedException("User email is not provided", EMAIL_PARAMETER, userEmail);
        }

        if (password == null) {
            logger.info("Request parameter {} is empty.", PASSWORD_PARAMETER);
            throw new BadRequestParameterDetectedException("User email is not provided", PASSWORD_PARAMETER, password);
        }

        return getAuthenticationManager().authenticate(new JWTUsernamePasswordAuthentication(userEmail, password));
    }

    /**
     * 
     * {@link #successfulAuthentication}메서드 구현은 {@code jwtManager}로부터 제공 받은 JWT 토큰을
     * {@link SecurityRestResponseHelper}를 이용하여 HTTP 응답 헤더에 전달한다. 이 메서드가 호출된다는 것은
     * 로그인의 성공을 의미하므로 FilterChain을 더 거칠 필요가 없어 바로 응답을 한다. 다만 이
     * {@link successfulAuthentication}메서드 구현은 spring security의
     * {@link AuthenticationSuccessHandler}를 사용하지 않기로 했다.
     * 
     * 유효성 검사 후의 메서드 진행 과정 중 {@link JwtException} 등의 예외가 발생할 경우 이는 인증 관련 예외가 아닌 인증
     * 성공 후처리 도중에 발생한 예외이기 때문에 이를 {@link InternalAuthenticationServiceException}으로
     * 변환하여 {@link #unsuccessfulAuthentication} 메서드에 전달한다.
     * 
     * 
     * 
     * @param authentication
     *            - 인증 성공을 나타내는 {@link Authentication} 객체이다. 이 메서드는 인자로 전달되는
     *            {@link Authentication} 객체의 {@link Authentication#getPrincipal}메서드가
     *            JWT building에 필요한 구성 요소를 담은 {@link UserAuthTokenDTO} 타입 오브젝트를
     *            반환한다고 가정하고 있다. 어떤 {@link AuthenticationProvider}가 선택되었는지 알 수 없으므로
     *            인자로 전달받은 {@link Authentication} 객체의 정체를 알 수 없다. 그러므로 예상한
     *            {@link Authentication}오브젝트가 전달되었는지를 검사하기 위해
     *            {@link UserAuthTokenDTOAuthentication}이라는 타입을 정의하게 되었다.
     *            {@link UserAuthTokenDTOAuthentication}을 구현한 하위 클래스의
     *            {@code #getPrincipal()} 메서드는 {@link UserAuthTokenDTO} 오브젝트를 반환함이
     *            보장된다. 이 필터 클래스와 함께 올바로 동작하길 바라는 {@link AuthenticationProvider} 구현의
     *            {@link AuthenticationProvider#authenticate} 메서드는 반드시
     *            {@link UserAuthTokenDTOAuthentication}타입 오브젝트를 반환해야한다.
     * 
     *            {@link AuthenticationProvider} 구현 클래스 이용에 조금이나마 유연성을 부여하기 위하여
     *            {@code authentication}은 특정 {@link Authentication} 구현 클래스 객체가 아닌 추상
     *            클래스 {@link UserAuthTokenDTOAuthentication}타입이면 모두 취급하기로 결정하였다.
     * 
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws ServletException, IOException {

        logger.info("Authentication success. Building JWT Token... ");

        try {
            Account account = Optional.ofNullable(authentication)
                    .filter(auth -> auth instanceof DefaultUserDetailsAuthentication)
                    .map(auth -> (Account) auth.getPrincipal()).orElseThrow(() -> {
                        logger.warn("Argument authentication to be instance of {} but {}",
                                DefaultUserDetailsAuthentication.class, authentication.getClass());
                        return new IllegalAuthenticationProviderUsedException("Invalid authentication detected.");
                    });

            validator.validate(account, UserAuthentication.class);

            TokenComponent tokenComponent = new TokenComponent(account.getId(),
                    UUID.randomUUID().toString().replace("-", ""));
            redisUserDAO.update(tokenComponent);

            UserAuthTokenDTO tokenDTO = new UserAuthTokenDTO(account, tokenComponent);

            String jwt = jwtServiceResolver.resolveJWTService(tokenDTO.getClass()).buildToken(tokenDTO);

            response.setStatus(HttpStatus.CREATED.value());
            response.setHeader(JWTService.JWT_HEADER_NAME, JWTService.JWT_PREFIX + jwt);

        } catch (AuthenticationServiceException e) {
            logger.warn("{} catched in successfulAuthentication()", e.toString());
            unsuccessfulAuthentication(request, response, e);
        }
    }

    /**
     * 
     * 인증 실패시에 호출되는 메서드이다. 이 {@link #unsuccessfulAuthentication} 메서드 구현은
     * {@link AuthenticationFailureHandler#onAuthenticationFailure}를 호출하여 응답을 위임한다.
     * 
     * @param failed
     *            - {@link #attemptAuthentication} 또는
     *            {@link #successfulAuthentication} 메서드에서 발생한 예외 객체이다. 해당 예외 객체는 이
     *            메서드가 {@link AuthenticationFailureHandler} 구현에 의해 처리된다.
     * 
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
