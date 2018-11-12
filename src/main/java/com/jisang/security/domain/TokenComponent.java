package com.jisang.security.domain;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.UserAuthentication;

/**
 * 
 * JWT token 인증에 필요한 정보를 담은 클래스이다. 이 클래스가 {@link Account}와 다른 점은 다음과 같다.
 * 
 * jisang 어플리케이션의 기본 유저 인증은 둘로 나눌 수 있다. 하나는 로그인이며 다른 하나는 로그인 유지를 위한 JWT token
 * 인증이다. {@link Account} 클래스가 로그인 인증(password 인증)에 사용되는 클래스라면 이 클래스는 JWT token
 * 인증에 사용되는 클래스이다.
 * 
 * redis를 선택하게 된 이유는 {@link RedisUserDAO}의 주석에 설명하였다.
 * 
 * 이 클래스의 오브젝트는 redis의 Hash 타입으로 저장된다. {@code user:1:jti} 또는
 * {@code user:1:phone}(물론 유저 phone 정보는 인증에서 사용되지는 않는다. 그러나 후에 유저 인증에 또 다른 정보가
 * 필요할 수 있을 것이다.)과 같은 key 네임으로 value를 찾는 것 보단 {@code user:1} 이라는 key의
 * {@code jti}라는 Hash key(field)를 조회하는 것이 더 낫다고 생각했다.
 * 
 * 전자인 String 타입의 key-value 구조의 경우는 유저를 구성하는 jti와 phone 정보가 Hash의 field로 정의하는
 * 것보다 더 분리되어 보인다. 다시 말해, Hash로 저장하는 방법의 경우가 더 이 정보들(jti, phone 등)이 유저에 대한 구성
 * 정보임이 더 명확하게 드러나는 것 같다.
 * 
 * 사실 앞서 말한 명확성의 경우 큰 차이가 나는 것은 아니다. 그러나 redis Documentation의 memory opimization
 * 파트(의 Use hashes when possible 이하)에 따르면 작은 field를 갖는 Hash가 위의 전자의 방법(예 :
 * user:1:jti)보다 더 메모리 효율적이라고 한다. 이런 점으로부터 redis Hash를 사용하기로 하였다.
 * 
 * 
 * @author leeseunghyun
 * @see https://redis.io/topics/memory-optimization
 *
 */
public class TokenComponent implements Serializable {

    private static final long serialVersionUID = -3363054857291053662L;

    @JsonIgnore
    private int id;

    @NotBlank(groups = { UserAuthentication.class, JWTBuilding.class })
    private String jti;

    public TokenComponent() {
    }

    public TokenComponent(int id, String jti) {
        this.id = id;
        this.jti = jti;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + ", jti=" + jti + "]";
    }

}
