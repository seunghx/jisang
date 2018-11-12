package com.jisang.security.authentication;

import java.io.IOException;
import java.util.Random;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import com.jisang.security.core.UsernameEndPointAuthentication;
import com.jisang.security.dto.AuthenticationNumberTokenDTO;
import com.jisang.security.core.DefaultUserDetailsAuthentication;
import com.jisang.security.core.JWTUsernamePasswordAuthentication;
import com.jisang.security.exception.BadRequestParameterDetectedException;
import com.jisang.security.exception.IllegalAuthenticationProviderUsedException;
import com.jisang.security.exception.handler.SecurityExceptionHandler;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;
import com.jisang.security.support.AuthenticationNumberNotificationProvider;


/**
 * 
 * 인증 번호 전송 방식은 보통 사용자의 핸드폰에 문자로 인증 번호를 전송하고 이를 검사하는 방법이 자주 사용되는 것 같다. 다른 방법으로는 푸시 알림으로 인증 번호를 
 * 전달한 후 사용자의 핸드폰에 전화연결을 하여 확인하는 방법도 있는 것 같다. 이메일로 인증 번호를 전송할 수도 있을 것이다. 무엇이 되었건 인증 번호를 받으려면
 * 인증 번호가 전송될 목적지에 대한 인증이 필요하다. 예를 들어 SMS로 인증 번호를 받아야 할 경우 사용자의 핸드폰 번호가 목적지가 될 것이다. 전달 받은 유저 이메일(id)
 * 을 이용해 데이터베이스로부터 목적지 정보를 가져온 후 이를 요청 파라미터로 전달된 목적지 정보와 비교해야한다.
 * 
 * 
 * (이 클래스가 인증 번호를 발급하는 필터라면 인증 번호를 인증하는 필터 {@link AuthenticationNumberAuthenticationFilter}가 있으며 임시 비밀번호를
 * 발급하는 필터도 있다. 인증 번호를 인증하는 필터 {@link AuthenticationNumberAuthenticationFilter} 클래스에서 바로 임시 비밀번호를 발급하지 않는 이유는
 * 후에 임시 비밀번호 발급 외에도 인증 번호의 인증이 필요한 시나리오가 또 생길 지도 모르기 때문에 인증 되었음만을 알려야 후에 다른 시나리오에서도 사용할 수 있기 때문이다.)
 * 
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class AuthenticationNumberEntryPointFilter extends AbstractAuthenticationProcessingFilter{

	
	// Static Fields
	//==========================================================================================================================

	private static final String EMAIL_PARAMETER = "email";
	private static final String NOTIFICATION_DESTINATION_PARAMETER = "destination";
	
	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(AuthenticationNumberEntryPointFilter.class);
	
	/** 인증 된 사용자에게 응답할 JWT token을 생성. 생성된 JWT token에는 인증 번호가 담겨 있다. 이는 후에 인증 번호에 대한 검사에 사용된다. */
	@Autowired
	private JWTServiceResolver jwtServiceResolver;
	/** 사용자에게 인증 번호를 전달하는 역할을 수행. SMS, mail, push알림 등의 방법이 사용될 수 있겠다. */
	@Autowired
	private AuthenticationNumberNotificationProvider authNumberNotificationProvider;

	
	// Constructors
	//==========================================================================================================================
	
	public AuthenticationNumberEntryPointFilter(RequestMatcher reqMatcher) {
		super(reqMatcher);
	}


	// Methods
	//==========================================================================================================================
	
	/**
	 * 
	 * 임시 비밀 번호 발급 전 입력 된 정보(이메일, 핸드폰 번호)가 올바른지 검사가 필요하다 이 {@link #attemptAuthentication} 구현은  
	 * {@link JWTUsernamePasswordAuthentication} 오브젝트를 {@link AuthenticationProvider}에 전달하며 실제 인증 작업을 위임한다. 
	 * 
	 * @throws UsernameNotFoundException HTTP 요청 파라미터 {@code email}이 전달되지 않았을 경우에 던져진다.
	 * @throws BadCredentialsException HTTP 요청 파라미터 {@code password}가 전달되지 않았을 경우에 던져진다. 
	 * @throws (UsernameNotFoundException, BadCredentialsException) : 잘못된 파라미터에 대하여 HTTP 400 응답으로 처리하여도 되겠지만 
	 * 		   {@link SecurityExceptionHandler} 구현의 편의를 위하여 {@link SecurityExceptionhandler#handle(RuntimeException)} 
	 * 		   메서드의 의해 HTTP 401 응답으로 처리되어지는 {@link UsernameNotFoundException, BadCredentialsException}을 던지도록 하였다.
	 * @throws AuthenticationException 이 메서드가 인증 작업을 위임하는 {@link AuthenticationProvider#authenticate()} 메서드에서 
	 * 								   발생하는 {@link AuthenticationException} 타입 예외들이 던져진다. 						 
	 * 									  
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {

		logger.info("Attempting authentication....");
		
		
		String userEmail = request.getParameter(EMAIL_PARAMETER);
		String notificationEndPoint = request.getParameter(NOTIFICATION_DESTINATION_PARAMETER);
		
		if(StringUtils.isEmpty(userEmail)) {
			logger.info("Request parameter {} is empty.", EMAIL_PARAMETER);
			throw new BadRequestParameterDetectedException("User email parameter missed.",EMAIL_PARAMETER, userEmail);
		}
		
		if(StringUtils.isEmpty(notificationEndPoint)) {
			logger.info("Request parameter {} is empty.", NOTIFICATION_DESTINATION_PARAMETER);
			throw new BadRequestParameterDetectedException("Destination parameter missed.", NOTIFICATION_DESTINATION_PARAMETER, notificationEndPoint);
		}
				
		return getAuthenticationManager()
					.authenticate(new UsernameEndPointAuthentication(userEmail, notificationEndPoint, request.getLocale()));
	}
	
	
	/**
	 * 
	 * {@link #successfulAuthentication}메서드 구현은 {@code jwtManager}로부터 제공 받은 JWT 토큰을 {@link SecurityRestResponseHelper}를 
	 * 이용하여 HTTP 응답 헤더에 전달한다. 이 메서드가 호출된다는 것은 인증 번호를 전송할 목적지가 올바르다는 의미이므로 FilterChain을 더 거칠 필요가 없어 바로 응답을 한다.
	 * 이 {@link successfulAuthentication}메서드 구현은 spring security의 {@link AuthenticationSuccessHandler}를 사용하지 않기로 했다. 
	 * 
	 * 유효성 검사 후의 메서드 진행 과정 중 {@link JwtException} 등의 예외가 발생할 경우 이는 인증 관련 예외가 아닌 인증 성공 후 처리 도중에 발생한 예외이기 때문에 이를 
	 * {@link InternalAuthenticationServiceException}으로 변환하여 {@link #unsuccessfulAuthentication} 메서드에 전달한다.
	 * 
	 * 
	 * @param authentication - 인증 성공을 나타내는 {@link Authentication} 객체이다.
	 * 						
	 * 		  			       
	 */
	@Override
	protected void successfulAuthentication( HttpServletRequest request
										   , HttpServletResponse response 
										   , FilterChain chain
										   , Authentication authentication ) throws ServletException, IOException {	
			
		try {
			if(!(authentication instanceof UsernameEndPointAuthentication)) {
 				logger.warn("Argument authentication to be instance of {} but {}", DefaultUserDetailsAuthentication.class,  authentication.getClass());
				throw new IllegalAuthenticationProviderUsedException("Invalid authentication detected.");
			}
			
			logger.info("Authentication success. Sending SMS to user... ");
			
			String userEmail = request.getParameter(EMAIL_PARAMETER);
			String notificationEndPoint = (String)authentication.getCredentials(); 
			String authenticationNumber = String.valueOf(new Random().nextInt(9999));
			
			authNumberNotificationProvider.sendAuthenticationNumber(notificationEndPoint, authenticationNumber);		
			
			AuthenticationNumberTokenDTO tokenDTO = new AuthenticationNumberTokenDTO();
			
			tokenDTO.setAuthenticationNumber(authenticationNumber);
			tokenDTO.setClientIPAddr(request.getRemoteAddr());
			tokenDTO.setUserEmail(userEmail);

			JWTService jwtService = jwtServiceResolver.resolveJWTService(tokenDTO.getClass());
			
			response.setStatus(HttpStatus.OK.value());
			response.setHeader(JWTService.JWT_HEADER_NAME, JWTService.JWT_PREFIX + jwtService.buildToken(tokenDTO));
						
		}catch(InternalAuthenticationServiceException e) {
			logger.warn("InternalAuthenticationServiceException catched in successfulAuthentication()");
			unsuccessfulAuthentication(request, response, e);
		}
	}	

	
	/**
	 * 
	 * 인증 실패시에 호출되는 메서드이다. 이 클래스의 {@link #unsuccessfulAuthentication} 메서드 구현은 
	 * {@link AuthenticationFailureHandler#onAuthenticationFailure}를 호출하여 응답 처리를 위임한다.
	 * 
	 * @param failed - {@link #attemptAuthentication} 메서드에서 발생한 예외 객체이다. 해당 예외 객체는 {@link SecurityExceptionHandler}에 
	 * 				   의해 처리된다.
	 * 				  
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response
																						, AuthenticationException exception) 
											 												throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Authentication request failed: {}", exception.getMessage());			
		}
		
		logger.debug("Delegating to AuthenticationFailureHandler...");
		
		getFailureHandler().onAuthenticationFailure(request, response, exception);
	}
}
