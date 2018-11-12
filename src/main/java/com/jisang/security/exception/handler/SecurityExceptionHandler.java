package com.jisang.security.exception.handler;



import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.jisang.security.dto.SecurityErrorDTO;


/**
 * spring security 관련 예외를 처리하고 예외에 해당하는 HTTP 응답 정보 담은 {@link SimpleResponseDTO}타입 오브젝트를 반환한다. spring security 
 * 관련 예외 {@link AuthenticationException} 또는 {@link AccessDeniedException} 타입이 아닌 예외는 이 인터페이스 구현에서 처리할 필요가 없다. 
 * security 단에서 발생할 수 있는 {@link ClassCastExcetpion}등의 {@link RuntimeException}은 {@link AuthenticationServiceException} 
 * 또는 {@link AuthorizationServiceException} 타입으로 변환될 것이기 때문에 정상적인 경우에 전달될 수가 없으며 이런 예외는 어플리케이션 밖으로 던져서
 * 잘못 구현된 부분을 찾아내야 하기 때문에 처리해서도 안된다.
 * 
 * 이 인터페이스는 자료형으로서 의미를 갖으며 실제 이 인터페이스를 구현하려는 클래스는 반드시 이 인터페이스를 구현한 추상 골격 클래스인
 * {@link AbstractSecurityExceptionHandler}를 계승하여야 한다. 인자 유효성 검사의 수행을 강제해야 하기 때문이다. 
 * 이 인터페이스의 타입 파라미터 T를 보면 {@code <T extends RuntimeException>} 으로 정의되어 있음을 알 수 있는데, spring security 관련 예외에 대한
 * 공통 조상 예외가 없기 때문에 이들을 모두 포함하는 계층상의 최하위에 존재하는 조상 예외 클래스인 {@link RuntimeException}을 파라미터 타입으로 정하게 되었고,
 * 이 때문에 불안전한 요소가 존재하기 때문에 유효성 검사가 필요하게 되었다.
 * 	
 *  
 * @author leeseunghyun
 * 
 */
public interface SecurityExceptionHandler<T extends RuntimeException> {
	

	// Static Fields
	//==========================================================================================================================


	// Methods
	//==========================================================================================================================

	
	/**
	 * 
	 * 예외를 처리하고 HTTP 응답 정보를 담은 {@link SimpleResponseDTO} 타입 오브젝트를 반환. 
	 * 
	 * @return 이 메서드 구현은 자신이 담당하는 HTTP 응답의 종류에 알맞은 {@link SimpleResponseDTO} 타입 객체를 반환해야 한다.
	 * 
	 */
	public SecurityErrorDTO handle(T e, Locale locale);
	
	
	/**
	 * 
	 *  이 인터페이스의 구현이 전달 받은 예외를 처리(supports) 가능한지 결정한다.
     *
	 */
	public  boolean supports(Class<? extends T> e);
	
	
	/**
	 * 
	 * 이 인터페이스 구현이 처리 가능한 예외 중 계층 구조상에서의 가장 하위에 위치한 예외에 대한 Class 오브젝트를 반환한다.
	 *
	 */
	public Class<? extends T> supportedException();
	
}
