package com.jisang.security.exception.handler;

import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.jisang.security.dto.SecurityErrorDTO;

/**/
/**
 * 
 * {@link SecurityExceptionHandler} 인터페이스에 대한 추상 골격 구현으로
 * {@link SecurityExceptionHandler} 구현이 안전하게 사용할 수 있도록 필요한 제약을 가하기 위해 정의하게 되었다.
 * 
 * 
 * {@link SecurityExceptionHandler} 구현을 이용하는 컴포넌트에서는 이 추상 클래스 타입 오브젝트를 사용하는 것이
 * 좋겠다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public abstract class AbstractSecurityExceptionHandler<T extends RuntimeException>
        implements SecurityExceptionHandler<T> {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link SecurityExceptionHandler} 구현이 처리 가능한 예외 타입 중 이들을 대표할 수 있는 예외 클래스(처리
     * 가능한 예외들 중 계층상 가장 하위에 존재하는 조상 예외)를 나타내는 {@link Class} 오브젝트이다.
     */
    private final Class<? extends T> representingException;
    protected final MessageSource msgSource;

    // Constructors
    // ==========================================================================================================================

    /**
     * 
     * 이 생성자의 인자 타입은 {@code Class<? extends T>}이고 이 클래스의 타입 파라미터 T는
     * {@code T extends RuntimeException}으로 정의되어 있다.
     * {@link SecurityExceptionHandler}와 이를 구현하는 클래스들은 security 단에서 발생한 예외를 처리하기 위해
     * 작성된 컴포넌트인데, 이 추상 클래스를 계승하는 서브 클래스가 잘못 작성되어 타입 파라미터 {@code T}를
     * {@link RuntimeException}으로 갖고 생성자에 {@link RuntimeException}이나
     * {@link ClassCastExcepton} 등의 {@link RuntimeException}의 서브 예외 클래스를 나타내는
     * {@link Class}오브젝트를 전달하는 경우가 가능할 수 있다. 이럴 경우를 대비해 생성자에서 {@link #validate} 메서드로
     * 인자를 전달해 인자의 타입을 검사한다. 그러므로 이 클래스를 계승할 경우 잘못 작성된
     * {@link SeucirtyExceptionHandler} 구현 객체는 생성될 수 없다. 컴파일러로부터 생성자 인자에 대한 타입 검사가
     * 진행되어 아예 클래스 작성조차 못하게 하면 더 좋았겠지만 현재 구현상 그런 검사는 불가능하므로 실행 시간 예외를 던진 후 잘못 작성된
     * {@link SecurityExceptionHandler} 구현 클래스의 오브젝트가 생성되었음을 알리는 정도로 만족해야겠다.
     * 
     * 
     * @param representingException
     *            {@link SecurityExceptionHandler} 구현이 처리할 수 있는 예외들 중 클래스 계층 상에 최하위에
     *            존재하는 공통 조상 예외여야만 한다.
     *
     * @throws RuntimeException
     *             {@link #validate}
     * 
     */
    public AbstractSecurityExceptionHandler(Class<? extends T> representingException, MessageSource msgSource) {
        this.representingException = representingException;
        this.msgSource = msgSource;

        validate(this.representingException);

    }

    // Methods
    // ==========================================================================================================================

    /**
     * {@link SecurityExceptionHandler} 구현이 전달 받은 예외를 처리(supports) 가능한지를 결정한다.
     * {@link SecurityExceptionHandler} 구현이 안정적으로 동작하기 위해 이 메서드는 오버라이드 불가능한 final로
     * 선언되었다. 이유는 오버라이딩이 가능할 경우 잘못 작성된 서브 클래스가 {@link RuntimeException} 등의 올바르지 않은
     * 예외를 처리(supports) 가능하다고 반환할 수 있기 때문이다.
     * 
     * @throws NullPointerException
     *             전달 받은 인자 {@code e}가 null일 경우 {@code e.getClass()}에 의해 이 예외가 던져진다.
     * 
     */
    @Override
    public final boolean supports(Class<? extends T> e) {
        return representingException.isAssignableFrom(e);
    }

    /**
     * {@code representingException}을 반환한다. 이 메서드는 예외를 처리할
     * {@link SecurityExceptionHandler}를 선택하는데 사용된다. 이 메서드가 final로 선언된 이유는 잘못 작성된
     * {@link SecurityExceptionHandler}의 구현이 올바르지 않은 {@link Class} 타입 오브젝트를 반환할 경우
     * 다른 컴포넌트에서 예외 처리를 위하여 {@link SecurityExceptionHandler} 구현 오브젝트를 선택할 때 문제가 발생하기
     * 때문이다.
     */
    @Override
    public final Class<? extends T> supportedException() {
        return representingException;
    }

    /**
     * 
     * {@link SecurityExceptionHandler} 인터페이스에서 설명한대로
     * {@link AuthenticationException}과 {@link AccessDeniedException}의 계층상의 최하위에
     * 존재하는 공통 조상 예외가 {@link RuntimeException} 밖에 없기 때문에
     * {@link SecurityExceptionHandler}의 타입 파라미터 T가
     * {@code <T extends RuntimeException>}으로 정의되어 있다. 이런 점으로부터 타입 파라미터 T를
     * {@link RuntimeException}으로 갖는 잘못된 {@link SecurityExceptionHandler} 구현이 있을 수
     * 있으므로 {@link SecurityExceptionHandler} 구현의 객체가 생성될 때 인자를 검사하여야 한다.
     * 
     * @param e
     *            정상적인 경우 인자 {@code e}는 {@link AuthenticationException} 또는
     *            {@link AccessDeniedException} 타입 {@link Class} 오브젝트이어야 한다.
     * @throws NullPointerException
     *             {@code e==null}의 경우에 발생.
     * @throws IllegalArgumentException
     *             e가 seucirty 관련 예외에 대한 {@link Class} 오브젝트가 아닌 경우 발생.
     * 
     */
    private void validate(Class<? extends T> e) {
        Objects.requireNonNull(e);

        logger.debug("Starting validation argument...");

        if (!(AuthenticationException.class.isAssignableFrom(e) || AccessDeniedException.class.isAssignableFrom(e))) {
            logger.warn("Invalid argument detected.");
            logger.warn("Argument e must be secuiry related exception Class but e : {}", e.toString());
            throw new IllegalArgumentException("Invalid argument detected.");
        }
    }

    // Abstract Methods
    // ==========================================================================================================================

    /**
     * 
     * 전달 받은 예외를 처리하고 해당 예외에 대응하는 HTTP 응답 코드와 메세지를 담은
     * {@link SecurityExceptionHandler} 타입 오브젝트를 반환.
     * 
     */
    @Override
    public abstract SecurityErrorDTO handle(T e, Locale locale);
}
