package com.jisang.security.core.userdetails;


import org.springframework.security.core.userdetails.UserDetails;

import com.jisang.security.dto.UserAuthTokenDTO;

/**/
/**
 *
 * 지상 어플리케이션의 유저 인증에 필요한 유저 정보를 제공한다. 이 인터페이스의 메서드 선언을 보면 {@link UserAuthTokenDTO}에 대한 getter와 setter가 선언 되었음을 
 * 알 수 있다. 인터페이스를 새로 정의하여 아래와 같은 메서드들을 선언하게 된 이유는 다음과 같다.
 * 
 * spring security에서 제공하는 {@link UserDetails#getUsername} 메서드는 정확히 유저의 이름만을 제공하기 위해 쓰이는 메서드는 아니기 때문에
 * (보통 user email 등 반환) user 정보에 정확히 1:1로 매칭 되지 않는다. 예를 들어 JWT payload 생성 과정 또는 {@link AuthenticationProvider}의 
 * 인증 절차에 유저의 아이디와 유저의 이름이 모두 필요할 경우 {@link UserDetails#getUsername()} 메서드는 사용을 헷갈리게 할 수 있다. 또한
 * {@link UserDetails}는 JWT payload 생성에 필요한 정보(예 : JTI)에 대한 접근자 메서드가 없다.
 * 
 * 그렇기에 user 정보와 JWT token 구성 정보를 모두 담고 있는 {@link UserAuthTokenDTO} 객체에 대한 getter와 setter만을 이 인터페이스에 선언하게 되었다. JWT 기반 인증
 * 과정에 이 인터페이스에서 새로 선언한 메서드들이 주로 이용될 것이라는 가정하에 차라리 {@link UserDetails} 자체를 어플리케이션에서 사용하지 않는 것은 어떨지에 대한 고민을
 * 해보았지만 {@link UserDetails}에 선언되어있는 의미있는 메서드들({@code UserDetails.isAccountNonExpired(), UserDetails.isAccountNonLocked()} 등)이
 * 후에 필요할 수도 있을 것 같아 이 인터페이스를 {@link UserDetails}의 서브 타입으로 결정하였다.
 * 
 * 
 * @author leeseunghyun
 * 
 */
public interface TokenDTOBasedUserDetails extends UserDetails{
	

	// Static Fields
	//==========================================================================================================================


	// Methods
	//==========================================================================================================================

	public void setTokenDTO(UserAuthTokenDTO tokenDTO);
	public UserAuthTokenDTO getTokenDTO();
}
