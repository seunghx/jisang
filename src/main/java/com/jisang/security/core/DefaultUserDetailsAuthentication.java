package com.jisang.security.core;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import com.jisang.security.core.userdetails.DefaultUserDetails;
import com.jisang.security.domain.Account;

/**/
/**
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class DefaultUserDetailsAuthentication implements JWTAuthentication {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = 3995337393863077425L;

    // Instance Fields
    // ==========================================================================================================================

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DefaultUserDetails userDetails;
    protected boolean authenticated;

    // Constructors
    // ==========================================================================================================================

    public DefaultUserDetailsAuthentication(DefaultUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public DefaultUserDetailsAuthentication(DefaultUserDetails userDetails, boolean authenticated) {
        this(userDetails);
        this.authenticated = authenticated;
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * {@code userDetails#getAuthorities()}를 반환.
     *
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    /**
     * {@link #getDetails} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public Object getDetails() {
        logger.warn("Unsupported operation getDetails() called.");
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getDetails()");
    }

    /**
     * {@link #getCredentials} 메서드는 현재 지원되지 않는다.
     */
    @Override
    public Object getCredentials() {
        logger.warn("Unsupported operation getCredentials() called.");
        throw new UnsupportedOperationException(getClass().getSimpleName() + ": Does not provide getCredentials()");
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    /**
     * 이 클래스의 {@code userDetails}의 {@link Account} 타입 프로퍼티를 반환.
     */
    @Override
    public Account getPrincipal() {
        return userDetails.getAccount();
    }

    /**
     * @return {@code userDetails#getUsername()}
     */
    @Override
    public String getName() {
        return userDetails.getUsername();
    }
}
