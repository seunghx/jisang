package com.jisang.security.core;

import java.util.Collection;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;

/**
 * 
 * 인증 번호를 발급하기 전 사용자가 요청한 정보가 정확한지, 예를 들어 SMS로 인증 번호를 발급하는 시나리오의 경우 SMS를 보낼 핸드폰 번호가 
 * 해당 id에 등록된 핸드폰 번호와 일치하는 지 검사할 필요가 있다. 이 클래스는 앞서 말한 인증 과정에 참여하는 클래스간의 데이터 전달을 목적으로
 * 정의한 {@link Authentication} 구현이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class UsernameEndPointAuthentication implements JWTAuthentication{

	
	// Static Fields
	//==========================================================================================================================

	private static final long serialVersionUID = 2130844419226388580L;

	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String username;
	private final String destination;
	private final Locale locale;
	

	// Constructors
	//==========================================================================================================================

	public UsernameEndPointAuthentication(String username, String destination, Locale locale) {	
		this.username = username;
		this.destination = destination;
		this.locale = locale;
	}


	// Methods
	//==========================================================================================================================

	/**
	 * 이 {@link Authentication} 구현의 {@link #getAuthorities} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		logger.warn("Unsupported operation getAuthorities() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getAuthorities()");
	}
	
	/**
	 * 이 {@link Authentication} 구현의 getCredentials() 메서드는 notification을 위해 사용될 유저의 목적지 (핸드폰 번호 등)정보를 반환한다.
	 */
	@Override
	public String getCredentials() {
		return destination;
	}

	/**
	 * 
	 * {@code locale}을 반환한다. 핸드폰 번호의 경우 국가마다 번호의 포맷이 다르기 때문에 만약 핸드폰 번호에 대한 인증이 필요할 경우 이 메서드로부터 
	 * {@link Locale}을 전달받을 필요가 있다. 이 메서드에서 locale을 반환하게 한 이유는 {@link Authentication#getDetails()}의 주석을 보면
	 * <pre> Stores additional details about the authentication request. </pre> 라고 명시되어 있었기 때문이다. 
	 * 
	 * 추가 적인 정보이기 때문에 email주소 확인(인증) 등을 수행하는 {@link AuthenticationProvider} 구현에서는 이 메서드를 이용하지 않으면 그만이다.
	 * {@link PhoneNumberAuthenticationProvider}에서는 이 메서드 호출이 필요하다.
	 *  
	 */
	@Override
	public Object getDetails() {
		return locale;
	}

	/**
	 *  이 {@link Authentication} 구현의 getCredentials() 메서드는 유저의 아이디 정보를 반환한다. 이 정보는 인증 번호를 요청한 유저의 목적지를 검색하는 데 사용된다.
	 */
	@Override
	public String getPrincipal() {
		return username;
	}

	/**
	 * 이 {@link Authentication} 구현의 {@link #isAuthenticated} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public boolean isAuthenticated() {
		logger.warn("Unsupported operation isAuthenticated() called.", this);	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide isAuthenticated()");
	}

	/**
	 * 이 {@link Authentication} 구현의 {@link #setAuthenticated} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		logger.warn("Unsupported operation setAuthenticated() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide setAuthenticated()");
	}

	@Override
	public String getName() {
		return getClass().getName() + "[username=" +username + ", destination=" + destination + ", locale=" + locale + "]";
	}

}
