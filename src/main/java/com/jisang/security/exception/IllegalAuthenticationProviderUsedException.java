package com.jisang.security.exception;

import org.springframework.security.authentication.InternalAuthenticationServiceException;



/**/
/**
 *
 * {@code Authentication Filter} 등에 올바르지 않은 {@link Authentication} 객체 등이 전달 되었을 경우 이는 인증을 진행한 {@link AuthenticationProvider}
 * 의 잘못된 구현 때문이다. 이 예외 클래스는 이런 사실을 나타내기 위해 작성된 예외이다.
 *
 * @author leeseunghyun
 *
 */
public class IllegalAuthenticationProviderUsedException extends InternalAuthenticationServiceException {
	
	
	// Static Fields
	//==========================================================================================================================
	
	private static final long serialVersionUID = 3057079398385700842L;
	
	
	// Instance Fields
	//==========================================================================================================================


	// Constructors
	//==========================================================================================================================
	
	public IllegalAuthenticationProviderUsedException(String message) {
		super(message);
	};
	
	public IllegalAuthenticationProviderUsedException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
	

	// Methods
	//==========================================================================================================================

}
