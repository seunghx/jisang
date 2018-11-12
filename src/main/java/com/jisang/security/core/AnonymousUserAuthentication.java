package com.jisang.security.core;

import java.util.Collection;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.jisang.security.dto.AnonymousUserAuthTokenDTO;

/**
 * 
 * 익명 사용자(로그인 하지 않은 사용자)의 인증 정보 {@link AnonymouUserAuthTokenDTO}를 담고 있는
 * {@link Authentication} 구현이다.
 * 
 * 단순히 {@link AnonymouUserAuthTokenDTO} 전달하는 일 외에 다른 메서드들은 지원하지 않는다. 스프링 시큐리티
 * {@code Authentication Filter}의 {@code @SuccessfulAuthentication} 메서드의 인자 타입이
 * {@link Authentication}이기 때문에 마음에 들지는 않지만 이런 구현을 하게 되었다.
 * 
 * 사실 위의 이유로 익명 사용자에 대한 인증(예 : 인증 번호를 통한 인증)은 spring security의
 * {@code Authentication Filter}가 아닌 서블릿 {@link Filter} 구현이나 controller에서 수행할까도
 * 생각해보았지만 일관성있게 인증과 관련된 부분은 모두 spring security의 {@code Authentication Filter}를
 * 사용하기로 하였다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class AnonymousUserAuthentication implements JWTAuthentication {

    private static final long serialVersionUID = -3910855120532002667L;

    private final Logger logger = LoggerFactory.getLogger(AnonymousUserAuthentication.class);

    private AnonymousUserAuthTokenDTO tokenDTO;

    public AnonymousUserAuthentication(AnonymousUserAuthTokenDTO tokenDTO) {
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
    public AnonymousUserAuthTokenDTO getPrincipal() {
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
     * @return {@code tokenDTO}의 {@code #getUserEmail()}
     */
    @Override
    public String getName() {
        return tokenDTO.getUserEmail();
    }

}
