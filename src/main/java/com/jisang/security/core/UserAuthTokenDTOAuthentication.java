package com.jisang.security.core;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.jisang.security.core.authority.GrantedAuthorityMapper;
import com.jisang.security.core.userdetails.TokenDTOBasedUserDetails;
import com.jisang.security.domain.Account;
import com.jisang.security.dto.UserAuthTokenDTO;


/**/
/**
 * 
 * {@link UserAuthTokenDTOAuthentication}의 기본 구현 클래스이다. 
 * 
 * @author leeseunghyun
 *
 */
public class UserAuthTokenDTOAuthentication implements JWTAuthentication{
	
	
	// Static Fields
	//==========================================================================================================================

	private static final long serialVersionUID = 5236531998922354548L;
	
	
	// Instance Fields
	//==========================================================================================================================

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final UserAuthTokenDTO tokenDTO;
	protected boolean authenticated;
	

	// Constructors
	//==========================================================================================================================
	
	public UserAuthTokenDTOAuthentication(UserAuthTokenDTO tokenDTO) {		
		this.tokenDTO = tokenDTO;
	}
	
	
	// Methods
	//==========================================================================================================================

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) {
		authenticated = isAuthenticated ;
	}

	/**
	 * 이 {@link Authentication} 구현의 {@link #getDetails()} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public Object getDetails() {
		logger.warn("Unsupported operation getDetails() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getDetails()");
	}

	/**
	 * 
	 * {@link GrantedAuthorityMapper}를 이용하여 {@code tokenDTO}가 포함하는 {@link Account} 오브젝트의 role 정보에 해당하는 
	 * {@link Collection<? extends GrantedAuthority>}를 반환.
	 * 
	 * @throws IllegalArgumentException {@link GrantedAuthorityMapper#resolveAuthority()}
	 * 
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {			
			return GrantedAuthorityMapper.resolve(tokenDTO.getAccount().getRole()).mapToGrantedAuthority();					
	}
	
	/**
	 * 이 {@link Authentication} 구현의 {@link #getCredentials} 메서드는 현재 지원되지 않는다.
	 */
	@Override
	public TokenDTOBasedUserDetails getCredentials() {
		logger.warn("Unsupported operation getCredentials() called.");	
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getCredentials()");
	}
	
	/**
	 * 이 {@link Authentication} 구현이 담고 있는 {@code tokenDTO} 프로퍼티를 반환.
	 */
	@Override
	public UserAuthTokenDTO getPrincipal() {
		return tokenDTO;
	}
	
    /**
     * 이 {@link Authentication} 구현의 이름과 이를 구성하는 필드 대한 정보를 반환.
     */
	@Override
	public String getName() {
		return getClass().getName() + "[tokenDTO=" + tokenDTO + ", authenticated=" + authenticated + "]";
	}
	
}


