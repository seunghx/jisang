package com.jisang.security.core;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;



/**/
/**
 * 
 * 내부적으로 JWT token을 나타내는 문자열을 담고 있는 {@link Authentication} 구현으로 이 클래스의 {@link #getCredentials}는 JWT 토큰 문자열을 반환한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class JWTTokenBasedAuthentication implements JWTAuthentication{
	
	
	// Static Fields
	//==========================================================================================================================
	
	private static final long serialVersionUID = -7784235082414367331L;
	

	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String jwt;
	private boolean authenticated;
	

	// Constructors
	//==========================================================================================================================


	public JWTTokenBasedAuthentication(String jwt) {
		
		this.jwt = jwt;
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
	 * {@code jwt}를 반환.
	 */
	@Override
	public Object getCredentials() {
		return jwt;
	}
	
    /**
	 * {@link #getDetails} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Object getDetails() {
		logger.warn("Unsupported operation getDetails() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getDetails()");
	}
	
   /**
     * {@link #getPrincipal} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Object getPrincipal() {
		logger.warn("Unsupported operation getPrincipal() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getPrincipal()");
	}
	
	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	 /**
     * 이 {@link Authentication} 구현의 이름과 이를 구성하는 필드 대한 정보를 반환.
     */
	@Override
	public String getName() {
		return getClass().getName() + "[jwt=" + jwt + "]";
	}

}
