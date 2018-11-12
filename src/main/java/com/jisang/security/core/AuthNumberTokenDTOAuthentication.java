package com.jisang.security.core;

import java.util.Collection;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.jisang.security.dto.AuthenticationNumberTokenDTO;

/**
 * 
 * 사용자의 인증 번호 전송과 관련된 인증 정보 {@link AuthenticationNumberTokenDTO} 오브젝트를 담고있는
 * {@link Authentication} 구현이다.
 * 
 * 단순히 {@link AuthenticationNumberTokenDTO} 전달 하는 일 외에 다른 메서드들은 지원하지 않는다. 스프링
 * 시큐리티 {@code Authentication Filter}의 {@code @SuccessfulAuthentication} 메서드의 인자
 * 타입이 {@link Authentication}이기 때문에 마음에 들지는 않지만 이런 구현을 하게 되었다.
 * 
 * 사실 위의 이유로 익명 사용자에 대한 인증(예 : 인증 번호를 통한 인증)은 spring security의
 * {@code Authentication Filter}가 아닌 서블릿 {@link Filter} 구현이나 controller에서 수행할까도
 * 생각해보았지만 일관성있게 인증과 관련된 부분은 모두 spring security의 {@code Authentication Filter}를
 * 사용하기로 하였다.
 * 
 * @author leeseunghyun
 *
 */
public class AuthNumberTokenDTOAuthentication implements Authentication {

    private static final long serialVersionUID = -7366572366954580158L;

    private final Logger logger = LoggerFactory.getLogger(AuthNumberTokenDTOAuthentication.class);

    private AuthenticationNumberTokenDTO tokenDTO;

    public AuthNumberTokenDTOAuthentication(AuthenticationNumberTokenDTO tokenDTO) {
        this.tokenDTO = tokenDTO;
    }

    /**
     * 이 클래스의 {@link #getAuthorities} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        logger.warn("Unsupported operation getAuthorities() called.");
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getAuthorities()");
    }

    /**
     * 이 클래스의 {@link #getCredentials} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public Object getCredentials() {
        logger.warn("Unsupported operation getCredentials() called.", this);
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getCredentials()");
    }

    /**
     * 이 클래스의 {@link #getDetails} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public Object getDetails() {
        logger.warn("Unsupported operation getDetails() called.", this);
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getDetails()");
    }

    /**
     * 이 클래스의 프로퍼티 {@code tokenDTO}를 반환한다.
     */
    @Override
    public AuthenticationNumberTokenDTO getPrincipal() {
        return tokenDTO;
    }

    /**
     * 이 클래스의 {@link #isAuthenticated()} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public boolean isAuthenticated() {
        logger.warn("Unsupported operation isAuthenticated() called.", this);
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide isAuthenticated()");
    }

    /**
     * 이 클래스의 {@link #setAuthenticated()} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        logger.warn("Unsupported operation setAuthenticated() called.", this);
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide setAuthenticated()");
    }

    /**
     * 이 {@link Authentication} 구현의 이름과 이를 구성하는 필드 대한 정보를 반환.
     */
    @Override
    public String getName() {
        return getClass().getName() + "[tokenDTO = " + tokenDTO + "]";
    }
}
