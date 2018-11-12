package com.jisang.security.core;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


/**/
/**
 * 폼 로그인 인증을 위해 사용될 목적의 {@link JWTAuthentication} 구현 클래스이다.
 * 폼 로그인 인증에 사용되므로 user의 아이디와 패스워드를 반환한다. 사실상 하는 일은 {@link UsernamePasswordAuthenticationToken}과 동일하지만 
 * 기능이 단순하기 때문에 따로 {@link UsernamePasswordAuthenticationToken}을 사용하거나 내부 프로퍼티로 두어 위임하는 방법으로 구현하지는 않았다.
 * 
 * 
 * @author leeseunghyun
 * 
 */
public class JWTUsernamePasswordAuthentication implements JWTAuthentication{
	
	
	// Static Fields
	//==========================================================================================================================

	private static final long serialVersionUID = 1538342564367762059L;

	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String username;
	private final String password;
	

	// Constructors
	//==========================================================================================================================

	public JWTUsernamePasswordAuthentication(String username, String password) {	
	
		this.username = username;
		this.password = password;
	}


	// Methods
	//==========================================================================================================================

	/**
	 * {@link #getAuthorities} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		logger.warn("Unsupported operation getAuthorities() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getAuthorities()");
	}
	
	/**
	 * 이 Authentication 구현의 getCredentials() 메서드는 폼 로그인에 사용될 유저의 패스워드 정보를 반환한다.
	 */
	@Override
	public String getCredentials() {
		return password;
	}

	/**
	 * {@link #getCredentials} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Object getDetails() {
		logger.warn("Unsupported operation getDetails() called.", this);	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getDetails()");
	}

	/**
	 *  이 Authentication 구현의 getCredentials() 메서드는 폼 로그인에 사용될 유저의 아이디 정보를 반환한다.
	 */
	@Override
	public String getPrincipal() {
		return username;
	}

	@Override
	public boolean isAuthenticated() {
		return false;
	}

	/**
	 * {@link #setAuthenticated} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		logger.warn("Unsupported operation setAuthenticated() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide setAuthenticated()");
	}

	 /**
     * 이 {@link Authentication} 구현의 이름과 이를 구성하는 필드 대한 정보를 반환.
     */
	@Override
	public String getName() {
		
		return getClass().getName() + "[username=" + username + ", password=" + password + "]";
	}

}
