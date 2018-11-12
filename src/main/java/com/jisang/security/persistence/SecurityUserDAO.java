package com.jisang.security.persistence;

import com.jisang.security.domain.Account;
import com.jisang.security.dto.UserAuthTokenDTO;

public interface SecurityUserDAO {

    // Methods
    // ==========================================================================================================================

    public Account find(String email);

    public void update(Account account);

    /**
     * 
     * 최초에는 security 단에서 로그인과 JWT token을 이용한 로그인 유지용 토큰 인증만을 수행할 목적이었다. 그러다 임시 비밀번호
     * 발급(핸드폰 인증 + 임시 비밀번호 메일 발송) 로직을 어플리케이션에 추가할 필요가 있어졌는데 핸드폰 인증도 결국 인증의 한 종류라는
     * 생각에 com.jisang.security.authentication 패키지에서 핸드폰 인증을 수행하기로 결정하였다. 최초의 계획대로
     * 일반적인 인증에 사용되는 JWT token 생성 정보를 담는 클래스 {@link UserAuthTokenDTO}과 관련된 데이터 접근만
     * 수행하는 이 인터페이스에 새로운 정보 {@code phone number}에 대한 접근 방법이 필요하였다.
     * {@link UserAuthTokenDTO} 내의 {@link Account}에 {@code phone}과 같은 프로퍼티를 추가할까도
     * 생각은 해보았지만 간단하게 아래와 같이 문자열 타입을 반환하게 하였다. 단순 비밀번호 필드 하나의 조회를 위해 새로운 객체를 생성하는 것은
     * 배보다 배꼽이 더 큰 것도 같다는 생각에 어느 정도 타협을 하였다. (책 등을 통해 배우기로는 도메인 위주의 방식이 좋다고 알고 있다.)
     * 
     */
    public String findPhoneNumber(String email);

}
