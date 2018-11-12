package com.jisang.security.dto;

import com.jisang.security.service.JWTService;

public interface TokenDTO {

    // Static Fields
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 이 인터페이스의 구현은 JWT 문자열을 꼭 반환할줄 알아야 한다. JWT parsing을 수행하려면 parsing할 JWT 문자열이 있어야
     * 하기 때문이다. JWT parsing을 수행할 {@link JWTService#parseToken(TokenDTO)} 메서드가 이
     * 인터페이스 타입 오브젝트를 받는 이유이기도 하다. 위 메서드가 단순히 JWT 문자열을 인자로 전달받지 않는 이유는 위 메서드가 내부적으로
     * 이용하는 {@link io.jsonwebtoken.JwtParser}의 parsing 관련 메서드가 parsing 하려는 JWT 문자열이
     * 나타내는 claims에 대한 검증을 함께 수행하기 때문에 JWT 문자열 외에 claim 검증에 필요한 정보 (예를 들어 사용자 IP
     * address 등) 또한 전달되어야 받아야 하기 때문이다.
     * 
     * 
     */
    public String getToken();
}
