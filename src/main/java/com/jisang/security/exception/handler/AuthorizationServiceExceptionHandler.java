package com.jisang.security.exception.handler;


import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.access.AuthorizationServiceException;

import com.jisang.security.dto.SecurityErrorDTO;


/**/
/**
 * 
 * {@link AuthorizationServiceException}을 처리하는 {@link SecurityExceptoinHandler} 구현. 
 * 
 * {@link AuthorizationServiceException} 타입 예외는 인가 처리 중에 발생한 {@link NullPointerException}과 같은 {@link RuntimeException}이 
 * 발생할 경우 생성되는 예외이기 때문에 이 클래스가 반환할 {@link SimpleResponseDTO}는 HTTP status 500 - Internal Server Error에 대한 정보를 포함한다.
 *
 *
 * @author leeseunghyun
 * 
 */
public class AuthorizationServiceExceptionHandler extends InternalServerErrorExceptionHandler<AuthorizationServiceException>{

	
	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================


	// Constructors
	//==========================================================================================================================

	public AuthorizationServiceExceptionHandler(MessageSource msgSource) {
		super(AuthorizationServiceException.class, msgSource);
	}
	

	// Methods
	//==========================================================================================================================

	/**
	 * 
	 * 이 메서드 구현은 단순히 500 - InternalServerError HTTP 응답을 나타내는 {@link SimpleResponseDTO}를 반환한다. 
	 * 항상 같은 응답 status code와 message를 갖고 있기 때문에 반환용 {@link SimpleResponseDTO}를 프로퍼티로 갖고 이를 반환하는 방법도 있겠지만,
	 * 이러한 방법은 외부에서 반환된 DTO의 내부를 바꿀 위험이 있기 때문에 이 메서드에서 반환할 객체를 매번 생성해서 반환하는 방법을 택하였다.
	 * 
	 */
	@Override
	public SecurityErrorDTO handle(AuthorizationServiceException e, Locale locale) {
		logger.debug("Handling AuthorizationServiceException...");
		
		logger.error("AuthorizationServiceException to be handled :", e);

		SecurityErrorDTO dto = new SecurityErrorDTO(getStatus());
		dto.setMessage(getMessage(locale));
		return dto;
	}

}
