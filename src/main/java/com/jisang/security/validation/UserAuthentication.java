package com.jisang.security.validation;

/**
 * 
 * 지상 어플리케이션의 기본 유저 인증(로그인 인증, 로그인 유지를 위해 유저의 상태를 저장하고 있는 JWT에 대한 인증) 과정에 참여하는
 * 컴포넌트에서 bean validation을 수행할 때, 이들 컴포넌트 모두에서 공동으로 검사되기를 필요로하는 프로퍼티에 대한 그룹핑용으로
 * 이 인터페이스가 사용된다.
 * 
 * @author leeseunghyun
 *
 */
public interface UserAuthentication {

}
