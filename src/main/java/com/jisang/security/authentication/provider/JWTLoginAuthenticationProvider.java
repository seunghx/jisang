package com.jisang.security.authentication.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jisang.security.core.UserAuthTokenDTOAuthentication;
import com.jisang.security.core.DefaultUserDetailsAuthentication;
import com.jisang.security.core.JWTAuthentication;
import com.jisang.security.core.JWTUsernamePasswordAuthentication;
import com.jisang.security.core.userdetails.DefaultUserDetails;
import com.jisang.security.dto.UserAuthTokenDTO;

/**/
/**
 * 
 * jisang 어플리케이션의 로그인 인증을 담당하는 AuthenticationProvider 구현 클래스.
 * 
 * 
 * @author leeseunghyun
 * 
 */
@Component("loginProvider")
public class JWTLoginAuthenticationProvider implements AuthenticationProvider {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Qualifier("default")
    private UserDetailsService service;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 폼 인증을 수행한다. service 오브젝트로부터 유저 정보를 가져온 후 비밀 번호를 비교한다.
     * 
     * @param authentication
     *            - {@code Authentication Filter}로부터 전달 받는 객체로 올바로 전달 되었을 경우
     *            {@link JWTAuthentication}을 구현한
     *            {@link JWTUsernamePasswordAuthentication} 타입 오브젝트이다.
     * @return 인증에 성공하였을 경우 내부적으로 {@link UserAuthTokenDTO} 오브젝트를 담고 있는
     *         {@link UserAuthTokenDTOAuthentication}을 반환.
     * 
     * @throws InternalAuthenticationServiceException
     *             전달받은 {@link Authentication} 객체가 담고 있는 유저 아이디, 패스워드가 null 또는 빈문자열일
     *             경우 발생.
     * @throws BadCredentialsException
     *             요청된 계정 정보의 비밀번호가 올바르지 않을 경우 던져진다.
     * @throws UsernameNotFoundException
     *             요청 파라미터(유저 아이디)에 해당하는 유저 정보가 없을 경우 던져진다.
     * 
     */
    @Override
    public DefaultUserDetailsAuthentication authenticate(Authentication authentication) throws AuthenticationException {
        validateAuthentication(authentication);

        UserDetails userDetails = service.loadUserByUsername((String) authentication.getPrincipal());

        if (!passwordEncoder.matches((String) authentication.getCredentials(), userDetails.getPassword())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Expected phone number to be : {}, but was {}.", userDetails.getPassword(),
                        passwordEncoder.encode((String) authentication.getCredentials()));
            }
            logger.info("Throwing BadCredentialsException...");

            throw new BadCredentialsException("Invalid password.");
        }
        return new DefaultUserDetailsAuthentication((DefaultUserDetails) userDetails);
    }

    /**
     * 
     * 전달받은 {@link Authentication} 객체가 올바로 구성되어 있는지를 검사한다. {@link #supports} 메서드에서
     * {@link JWTUsernamePasswordAuthentication}을 {@code supports} 한다고 하였으므로 이 메서드에
     * 전달된 {@code authentication} 오브젝트는 당연히
     * {@link JWTUsernamePasswordAuthentication}일 것이라는 가정하에 이 메서드 코드에 존재하는 타입 변환은
     * 안전하다고 생각하였다.
     * 
     */
    private void validateAuthentication(Authentication authentication) {
        if (StringUtils.isEmpty((String) authentication.getPrincipal())) {
            throw new InternalAuthenticationServiceException(
                    "Illegal argument detected, username must not be empty String.");
        }
        if (StringUtils.isEmpty((String) authentication.getCredentials())) {
            throw new InternalAuthenticationServiceException(
                    "Illegal argument detected, password must not be empty String.");
        }
    }

    /**
     * 
     * 인자로 전달 받은 {@link Class} 오브젝트가 이 클래스가 인증 과정에 처리할 수 있는 {@link Authentication}타입
     * 구현 오브젝트인지를 검사한다. 정확히는 폼 인증에 필요한 정보인 유저 아이디, 패스워드를 담고 있는
     * {@link JWTUsernamePasswordAuthentication} 타입일 경우 지원 가능함에 대한 의미로 true를 반환한다.
     * 
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JWTUsernamePasswordAuthentication.class.isAssignableFrom(authentication);
    }
}
