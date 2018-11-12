package com.jisang.security.core.userdetails;


import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.jisang.security.core.authority.GrantedAuthorityMapper;
import com.jisang.security.domain.Account;
import com.jisang.security.dto.UserAuthTokenDTO;


/**/
/**
 *
 * 어플리케이션의 유저 인증과 관련하여 사용 될 유저 정보, 그리고 이와 더불어 JWT token 구성에 필요한 정보를 담은 {@link TokenDTOBasedUserDetails} 구현이다.
 * 내부적으로 {@link UserAuthTokenDTO} 타입 객체를 담고 있는데 그 이유는 {@link TokenDTOBasedUserDetails}에 설명되어 있다.
 * 
 * @author leeseunghyun
 * 
 */
public class UserAuthTokenDTOBasedUserDetails implements TokenDTOBasedUserDetails{
	
	
	// Static Fields
	//==========================================================================================================================

	private static final long serialVersionUID = -4813717918248242345L;

	
	// Instance Fields
	//==========================================================================================================================
	
	private UserAuthTokenDTO tokenDTO; 
	
	
	// Constructors
	//==========================================================================================================================
	
	public UserAuthTokenDTOBasedUserDetails(UserAuthTokenDTO tokenDTO) {
		this.tokenDTO = tokenDTO;
	}


	// Methods
	//==========================================================================================================================
	
	/**
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

	@Override
	public String getPassword() {
		return tokenDTO.getAccount().getPassword();
	}

	@Override
	public String getUsername() {
		return String.valueOf(tokenDTO.getAccount().getId());
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	/**
	 * Override from {@link TokenDTOBasedUserDetails}
	 */
	@Override
	public void setTokenDTO(UserAuthTokenDTO tokenDTO) {
		this.tokenDTO = tokenDTO;
	}
	
	/**
	 * Override from {@link TokenDTOBasedUserDetails}
	 */
	@Override
	public UserAuthTokenDTO getTokenDTO() {
		return tokenDTO;
	}
	
}
