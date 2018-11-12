package com.jisang.security.core;

import org.springframework.security.core.Authentication;

/**/
/**
 * 
 * JWT 기반 인증에서 사용될 이 어플리케이션의 모든 {@link Authentication}구현의 슈퍼 타입이다. 현재로서는 존재의 의미는 없으나, 프레임워크에서 제공하는 
 * 오브젝트({@link Authentication})보다는 어플리케이션에 이용되는 {@link Authentication} 구현 클래스들을 더 구체적으로 아우르는 자료형이 있으면 
 * 후에 좋을 지도 모를 것 같아 일단 만들어 두었다.
 * 
 * @author leeseunghyun
 * 
 *
 */
public interface JWTAuthentication extends Authentication {

	
	// Static Fields
	//==========================================================================================================================


	// Methods
	//==========================================================================================================================

}
