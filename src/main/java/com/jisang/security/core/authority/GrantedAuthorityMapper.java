package com.jisang.security.core.authority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import static com.jisang.config.code.CodeBook.UserType;
import static com.jisang.config.code.CodeBook.UserType.NORMAL_USER;
import static com.jisang.config.code.CodeBook.UserType.MANAGER_USER;
import static com.jisang.config.code.CodeBook.UserType.ADMIN_USER;

/**/
/**
 * 
 * {@link CodeBook.NORMAL_USER}와 같은 유저의 타입을 의미하는 카테고리성 코드와 spring security에서
 * 사용되는 {@link GrantedAuthority}의 {@link Collection}을 매핑한다. static utility 클래스를
 * 정의하여 메서드에서 if-else 문을 이용해 매핑시키나 아래와 같이 enum으로 구현하나 user role이 추가될 경우 혹은 삭제될
 * 경우 수정이 불가피함은 동일하나 if-else 문으로 구성된 static 메서드 구현의 경우 user role의 수가 늘아날 수록 메서드가
 * 매우 비대해져 가독성에 문제가 될 수 있다고 생각하여 아래와 같은 enum 전략 패턴을 사용하였다. 또한 if-else문을 이용한 메서드를
 * 정의하였을 경우 {@link UserType.NORMAL_USER}를 나타내는 "1"이라는 문자열 리터럴이 if 조건문상에 비교대상으로
 * 들어갈텐데, 해당 메서드의 코드만 봐서는 이 문자열 리터럴이 무엇을 의미하는지 알 수가 없다는 단점이 있다.
 * 
 * 사실 초기에는 이 클래스의 기능을 {@link CodeBook.UserType} enum 클래스에서 수행하였으나 security단과 이외의
 * 웹 어플리케이션 단을 가능한한 분리시키고 싶어 이 클래스를 새로 정의하게 되었다.
 * 
 * 다른 방법으로는 {@link Map} 타입 빈을 생성하고 이 맵에서 {@link #mapToGrantedAuthority()} 메서드와
 * 같은 역할을 수행하게 하는 방법도 있다. 이럴 경우에 user role이 추가되어도 {@link @Configuration} 클래스만
 * 수정하면 되므로 더 괜찮은 방법인 것도 같다.
 * 
 * 무언가 아쉬움이 있어 마음에 들지 않는다. 쓸데 없이 enum 클래스를 사용한 것은 아닌가 싶다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public enum GrantedAuthorityMapper {

    ROLE_USER(NORMAL_USER.getCode()) {
        @Override
        public Collection<GrantedAuthority> mapToGrantedAuthority() {
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved authorities : ROLE_USER.");
            }
            return AuthorityUtils.createAuthorityList("ROLE_USER");
        }
    },

    ROLE_MANAGER(MANAGER_USER.getCode()) {
        @Override
        public Collection<GrantedAuthority> mapToGrantedAuthority() {
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved authorities : ROLE_USER, ROLE_MANAGER.");
            }
            return AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_MANAGER");
        }
    },

    ROLE_ADMIN(ADMIN_USER.getCode()) {
        @Override
        public Collection<GrantedAuthority> mapToGrantedAuthority() {
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved authorities : ROLE_USER, ROLE_MANAGER, ROLE_ADMIN.");
            }
            return AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN");
        }
    };

    // Static Fields
    // ==========================================================================================================================

    private static final Map<String, GrantedAuthorityMapper> stringToEnum = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GrantedAuthorityMapper.class);

    // Instance Fields
    // ==========================================================================================================================

    private String symbol;

    // Static block
    // ==========================================================================================================================

    static {
        for (GrantedAuthorityMapper roleMapper : values()) {
            stringToEnum.put(roleMapper.symbol, roleMapper);
        }
    }

    // Constructors
    // ==========================================================================================================================

    GrantedAuthorityMapper(String symbol) {
        this.symbol = symbol;
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * @return 이 클래스의 enum 오브젝트 중 인자로 전달 받은 {@code role}에 해당하는 오브젝트를 반환한다.
     * 
     * @throws InternalAuthenticationServiceException
     *             전달 받은 인자 {@code role}이 어플리케이션에서 허용하는 user role에 대한 코드 값이 아닌 경우 이
     *             예외가 던져진다. {@link IllegalArgumentException} 등의 예외가 보다 더 적합해 보일 수도
     *             있으나 security 단에서의 인증 과정 중 발생하는 {@link RuntimeException} 타입 예외는
     *             {@link AuthenticationFailureHandler}를 통해 처리되도록 하기위해 이 예외 클래스로
     *             변환하는 것이 옳다고 생각하였다.
     * 
     * 
     */
    public static GrantedAuthorityMapper resolve(String role) {

        return Optional.ofNullable(role).map(userRole -> stringToEnum.get(userRole)).orElseThrow(() -> {
            logger.warn("Argument role is invalid. Argument role : {}.", role);
            logger.warn("Check user role data in JWT token or Database.");

            throw new InternalAuthenticationServiceException("Argument role is invalid.");
        });
    }

    @Override
    public String toString() {
        return symbol;
    }

    public abstract Collection<GrantedAuthority> mapToGrantedAuthority();
}
