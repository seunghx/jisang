package com.jisang.security.validation;

/**
 * 
 * 지상 어플리케이션에서 로그인 상태 유지를 위한 정보를 담은 JWT token에 대한 인증에 참여하는 컴포넌트에서 bean validation을 수행할 때, 이들 컴포넌트에서 공동으로 
 * 검사되기를 원하는 프로퍼티에 대한 그룹핑 용으로 이 인터페이스가 사용된다.
 * 
 * @author leeseunghyun
 *
 */
public interface JWTBasedUserAuthentication extends UserAuthentication {

}
