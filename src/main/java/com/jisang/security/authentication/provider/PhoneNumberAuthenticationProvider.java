package com.jisang.security.authentication.provider;

import java.text.ParseException;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.Formatter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jisang.security.core.JWTAuthentication;
import com.jisang.security.core.UsernameEndPointAuthentication;
import com.jisang.security.exception.InvalidPhonenumberException;
import com.jisang.security.exception.SecurityUnsupportedLocaleException;
import com.jisang.security.exception.PhonenumberFormatException;
import com.jisang.security.persistence.SecurityUserDAO;
import com.jisang.support.conversion.UnsupportedLocaleException;


/**
 * 
 * 사용자의 핸드폰에 인증 번호를 전송하기 전 사용자가 입력한 핸드폰과 이메일 정보가 동일한 유저의 핸드폰/이메일 정보인지를 검사할 필요가 있다.
 * 이 {@link AuthenticationProvider} 구현은 위의 작업을 수행한다. 
 * 
 * @author leeseunghyun
 *
 */
@Component("phoneNumberProvider")
public class PhoneNumberAuthenticationProvider implements AuthenticationProvider {
	
	
	// Static Fields
	//==========================================================================================================================
	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(PhoneNumberAuthenticationProvider.class);
	@Autowired
	private SecurityUserDAO userDAO;
	@Autowired
	@Qualifier("phoneNumberFormatter")
	private Formatter<String> phoneNumberFormatter;

	
	// Constructors
	//==========================================================================================================================
	

	// Methods
	//==========================================================================================================================
		
	/**
	 * 
	 * DAO 오브젝트로부터 유저 핸드폰 번호를 가져온 후 이를 비교한다. 
	 * 
	 * @param   authentication - {@code Authentication Filter}로부터 전달 받는 객체로 올바로 전달 되었을 경우 
	 * 					 		 {@link JWTAuthentication}을 구현한 {@link UsernameEndPointAuthentication} 타입 오브젝트이다. 
	 * 	  
	 * @throws  InternalAuthenticationServiceException  전달받은 {@code authentication} 객체가 담고 있는 유저 아이디, 핸드폰 번호가 
	 * 			null 또는 빈문자열일 경우 발생.
	 * @throws  InvalidPhonenumberException 요청된 유저 아이디 정보에 해당하는 유저가 없거나 핸드폰 번호가 일치하지 않을 경우 던져진다.
	 * 			 
	 */
	@Override
	public UsernameEndPointAuthentication authenticate(Authentication authentication) throws AuthenticationException {
		
			validateAuthentication(authentication);
			
			String username = (String)authentication.getPrincipal();
			String phoneNumberFromDB = 
									Optional.ofNullable(userDAO.findPhoneNumber(username))
										 	.orElseThrow(() -> 
										 		new InvalidPhonenumberException("Username does not exist. username : " + username));
			String phoneNumberFromRequest = null;
			try {
				phoneNumberFromRequest = 
						phoneNumberFormatter.parse((String)authentication.getCredentials(), (Locale)authentication.getDetails());
			
			}catch(UnsupportedLocaleException e) {
				if(logger.isInfoEnabled()) {
					logger.info("{} throwed {}.", phoneNumberFormatter, e.toString());
				}
				throw new SecurityUnsupportedLocaleException(e.toString(), e, (Locale)authentication.getDetails());
				
			}catch(ParseException e) {
				if(logger.isInfoEnabled()) {
					logger.info("{} throwed {}.", phoneNumberFormatter, e.toString());
				}
				throw new PhonenumberFormatException(e.toString(), e, (String)authentication.getCredentials());
			}
			
			
			if(!phoneNumberFromRequest.equals(phoneNumberFromDB)){
				if(logger.isDebugEnabled()) {
					logger.debug("Requested user phone does not matched.");
					logger.debug("Expected phone number to be : {}, but was {}.", phoneNumberFromDB, phoneNumberFromRequest);
					logger.debug("Throwing InvalidPhoneNumberException...");
				}
				throw new InvalidPhonenumberException("Received user phone number does not matched.");
			}
		
			return new UsernameEndPointAuthentication(username, phoneNumberFromRequest, (Locale)authentication.getDetails());
	}
			
	
	/**
	 * 
	 * 전달받은 {@link Authentication} 객체가 올바로 구성되어 있는지를 검사한다. {@link #supports} 메서드에서 
	 * {@link UsernameEndPointAuthentication}을 {@code supports} 한다고 하였으므로 이 메서드에 전달된 {@code authentication} 오브젝트는
	 * 당연히 {@link UsernameEndPointAuthentication}일 것이라는 가정하에 이 메서드 코드에 존재하는 타입 변환은 안전하다고 생각하였다.
	 * 
	 */
	private void validateAuthentication(Authentication authentication) {
		
		if(StringUtils.isEmpty((String)authentication.getPrincipal())) {
			throw new InternalAuthenticationServiceException("Illegal argument detected, user email must not be empty String.");
		}
		
		if(StringUtils.isEmpty((String)authentication.getCredentials())) {
			throw new InternalAuthenticationServiceException("Illegal argument detected, user phone must not be empty String.");
		}
		
		if(!(authentication.getDetails() instanceof Locale)) {
			throw new InternalAuthenticationServiceException("Illegal argument detected, authentication#getDetails() must return java.util.Locale");
		}
	}
	
	/**
	 * 
	 * 인자로 전달 받은 {@link Class} 오브젝트가 이 클래스가 인증 과정에 처리할 수 있는 {@link Authentication}타입 구현 오브젝트인지를 검사한다.
	 * 정확히는 휴대폰 인증에 앞서 필요한 정보인 유저 아이디, 핸드폰 번호를 담고 있는 {@link UsernameEndPointAuthentication} 타입일 경우
	 * 지원 가능함에 대한 의미로 true를 반환한다.
	 * 
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return UsernameEndPointAuthentication.class.isAssignableFrom(authentication);		
	}
}
