package com.jisang.security.exception.handler;

import java.util.Locale;
import java.util.Objects;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jisang.security.dto.SecurityErrorDTO;
import com.jisang.security.exception.AuthenticationNumberExpiredException;
import com.jisang.security.exception.InvalidPhonenumberException;
import com.jisang.security.exception.JWTAuthenticationException;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**/
/**
 * 
 * spring security의 {@link AuthenticationException}타입 클래스에 대한
 * {@link SecurityExceptionHandler} 구현이다.
 * 
 * 
 * @author leeseunghyun
 * 
 */
public class AuthenticationExceptionHandler extends AbstractSecurityExceptionHandler<AuthenticationException> {

    // Static Fields
    // ==========================================================================================================================

    /**
     * 이 {@link SecurityExceptionHandler} 구현이 처리하는 예외에 해당하는 HTTP status 코드.
     */
    private static final int HTTP_STATUS_UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public AuthenticationExceptionHandler(MessageSource msgSource) {
        super(AuthenticationException.class, msgSource);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 이 메서드 구현의 내부를 보면 인자의 타입을 검사함을 알 수 있는 데 현재 구현처럼 단순히 반환될 DTO에 메세지 설정 정도만 할
     * 것이라면, 예를 들어 이 클래스에 {@link Map} 타입 필드를 두어 동적으로 처리가 가능하도록 할 수도 있지만 그렇게 하지 않고
     * if-else문을 이용하여 타입 검사를 수행하였다.
     * 
     * 이유는, 단순하게 {@link Map} 타입 필드를 두고 이 필드를 검색해 인자에 따른 반환 메세지를 mapping하여 처리하는 방법은
     * 현재의 이 메서드 구현에서 수행하는 (단순한 메세지 설정만 하는) 작업보다 더 디테일한 작업을 해야할 경우 이를 처리하기에는 어려운 부분이
     * 있다.맵을 이용하여 디테일한 동작이 가능하게 하려면 또 다른 컴포넌트를 생성해서 이를 매핑해주어야 할텐데, 이렇게 할 경우 실제 하는 일에
     * 비해 구조만 복잡해질 수 있기 때문이다. (현재 이 메서드 구현을 보면 알 수 있지만 실제 예외를 처리하는 작업이 단순하다. 처리해야할
     * 작업이 추가 되더라도 매우 복잡한 작업이 될 것이라고 생각하지는 않는다.) 또한 이미 프레임워크에 의해 정해진
     * {@link AuthenticationException} 타입 예외는 더 추가되는 일이 적을 것이고 추가 되더라도 최소한 그 시점까지는
     * 어플리케이션에서 그 추가된 예외가 이용되지 않을 것이기 때문에 이 메서드가 오동작할 일이 없으므로 이런 정적인 검사로 적당하다고
     * 생각하였다. 또한 spring의 컨트롤러에서 발생한 예외를 처리하는
     * {@link ResponseEntityExceptionHandler#handleException} 메서드도 if-else 문으로 예외
     * 타입을 검사하는 것으로보아 이런 방법이 나빠보이지는 않는다.
     * 
     * 추후에 만약 예외에 대한 처리 작업이 많이 복잡해진다면 다른 컴포넌트를 작성해 동적인 위임을 하는 방법으로 변경해봐야겠다.
     * 
     * 
     * 
     * @throw NullPointerException {@code e==null}의 경우 발생.
     * @throws IllegalArgumentException
     *             정체 모를 {@link AuthenticationException} 타입 예외가 전달 될 경우 발생한다. 새로 구현
     *             되었거나 프레임워크에 새로 추가 된 {@link AuthenticationException}타입 예외가 전달 되었을
     *             때 발생할 수 있다.
     * 
     */
    @Override
    public SecurityErrorDTO handle(AuthenticationException e, Locale locale) {
        Objects.requireNonNull(e);

        logger.debug("Handling exception...");

        logger.error("AuthenticationException to be handled :", e);

        SecurityErrorDTO dto = new SecurityErrorDTO(HTTP_STATUS_UNAUTHORIZED);

        if (e instanceof UsernameNotFoundException) {
            dto.setMessage(msgSource.getMessage("security.excetpion.UsernameNotFoundException", null,
                    "Invalid user id or password", locale));
        } else if (e instanceof JWTAuthenticationException) {
            dto.setMessage(msgSource.getMessage("security.exception.JWTAuthenticationException", null,
                    "Untrustful JWT token. login required.", locale));
        } else if (e instanceof InvalidPhonenumberException) {
            dto.setMessage(msgSource.getMessage("security.exception.BadCredentialsWithPhonenumberException", null,
                    "Invalid user id or phone number", locale));
        } else if (e instanceof BadCredentialsException) {
            dto.setMessage(msgSource.getMessage("security.exception.BadCredentialsException", null,
                    "Invalid user id or password", locale));
        } else if (e instanceof InsufficientAuthenticationException) {
            dto.setMessage(msgSource.getMessage("security.excetpion.InsufficientAuthenticationException", null,
                    "Login required.", locale));
        } else if (e instanceof AuthenticationCredentialsNotFoundException) {
            dto.setMessage(msgSource.getMessage("security.exception.AuthenticationCredentialsNotFoundException", null,
                    "Server error occurred", locale));
        } else if (e instanceof AuthenticationNumberExpiredException) {
            dto.setMessage(msgSource.getMessage("security.exception.AuthenticationNumberExpiredExceptionException",
                    null, "Time limit expired", locale));
        } else {
            logger.warn("Argument e must be direct subtype of AuthenticationException. But argument e : {}.",
                    e.toString());
            throw new IllegalArgumentException("Unknown exception argument detected.");
        }
        return dto;
    }
}
