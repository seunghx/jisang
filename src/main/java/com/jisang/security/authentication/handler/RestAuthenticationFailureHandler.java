package com.jisang.security.authentication.handler;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisang.security.dto.SecurityErrorDTO;
import com.jisang.security.exception.handler.AbstractSecurityExceptionHandler;
import com.jisang.security.exception.handler.SecurityExceptionHandler;

/**/
/**
 * 
 * 인증이 실패했을 때 이용 될 수 있는 클래스로 예외를 처리하고 해당 예외에 알맞은 HTTP 응답을 설정한다. {@link Set} 타입 {@code exceptionHandlers} 필드를 
 * 통해 예외 타입에 따라 적절한 응답 설정을 할 수 있도록 작업을 위임한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
	
	
	// Static Fields
	//==========================================================================================================================
	
	/**
	 * {@link AuthenticationException} 타입 예외 처리를 담당하는 {@link SecurityExceptionHandler} 구현 객체를 저장하는 list.
	 * 
	 * 
	 * 동적인 추가를 고려하여 작성한 이 클래스의 메서드 {@link #addExceptionHandler}에서 문제가 발생하지 않도록 동시 쓰기에 안전한 자료구조를 작성하였으나,
	 * 생각을 해보니 동적으로 추가될 일이 없을 것 같다. 일단 수정은 하지 않았다.
	 */
	private final List<SecurityExceptionHandler<AuthenticationException>> exceptionHandlers = new CopyOnWriteArrayList<>();
	
	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ObjectMapper objMapper = new ObjectMapper();


	// Constructors
	//==========================================================================================================================
	
	public RestAuthenticationFailureHandler() {
		this.objMapper.writerWithDefaultPrettyPrinter();
	}

	// Methods
	//==========================================================================================================================

	/**
	 * 
	 * 
	 * 인증이 실패될 때 {@code Authentication Filter}의 {@code #unsuccessfulAuthentication()} 메서드에 의해 호출되는 메서드이다.
	 * {@code exceptionHandlers} 필드에 보관된 예외 객체를 처리할 {@link SecurityExceptionHandler}를 선택하고 예외 처리를 위임한다. 
	 * 또한 이로부터 전달 받은 인증 관련 예외를 처리하고 예외에 해당하는 응답 내용을 인자로 전달 된 {@code response} 오브젝트에 설정한다.
	 * REST 어플리케이션이기 때문에 오류 페이지 등으로 redirect하지 않고 {@link SecurityExceptionHandler}로부터 전달 받은 오류 응답 정보를 
	 * {@code response} 오브젝트에 써야한다. (그러나 이 메서드에서 예외 및 응답 처리 중 또 다른 {@link RuntimeException}이 발생할 경우에는 
	 * web.xml에 지정한 예외 페이지로 예외가 전달된다.)
	 * 
	 * 
	 * 이 메서드에서 사용하는 stream 라이브러리의 {@code map()} 메서드 내부에는 {@code handler.handle(ex)}과 같은 코드가 있다. 
	 * 이 클래스의 {@code exceptionHandlers} 필드가 {@code SecurityExceptionHandler<AuthenticationException>} 타입 오브젝트를 담고 있기
	 * 때문에 {@link handler.handle()} 메서드의 인자 타입이 {@link Authentication} 타입임을 알 수 있다.
	 * {@code exceptonHandlers} 필드에는 {@link AuthenticationExceptionHandler} 뿐만 아니라 {@link AuthenticationServiceExceptionHandler} 
	 * 등 보다 하위 타입 예외를 처리하는 {@link SecurityExceptionHandler} 구현 객체도 추가될 텐데, 그럼에도 불구하고 {@code handle()} 메서드의 인자가 
	 * {@link Authentication}으로 선언되어 있어도 안전하게 동작이 가능하다. 그 이유는 {@code handle()} 메서드가 실행되기 이전에 전달 받은 예외 객체를
	 * {@code supports()} 가능한지에 대한 필터링이 이미 이루어졌기 때문이다. ({@link AbstractSecurityExceptionHandler#supports} 메서드가 final로
	 * 선언된 이유이기도 하다.)
	 * 
	 * 참고 : 이 메서드 내부에서 사용하는 stream 라이브러리의 {@code #min()} 메서드에 전달 되는 {@link Comparator} 구현은 {@code exceptionHandlers} 
	 * 		 필드가 보관 중인 {@link SecurityExceptionHandler} 오브젝트 각각이 대표적으로 처리 가능하다고 반환한 예외 중 클래스 
	 * 		 ({@code AbstractSecurityExceptionHandler.supportedException}메서드가 반환하는 예외) 중 클래스 계층 상에서 더 낮은 계층에 존재하는 
	 * 		 예외 객체를 반환한 {@link SeucurityExceptionHandler}에 더 높은 우선순위를 주도록 구현되어 있다. 
	 * 
	 * 		 주의해야할 점은 이 {@link Comparator} 구현은 일반적인 경우에 추이성(transitivity)을 만족하지 못한다. 그러나 
	 * 		 {@link SecurityExceptionHandler#supports} 메서드가 true를 반환한({@code stream.filter(handler -> handler.supports(ex)}) 
	 * 		 경우에 대해서만 이 {@link Comparator} 구현이 사용되는데, 이런 경우에는 추이성이 만족된다. 이유는 전달 받은 예외에 대하여 
	 * 		 {@code handler.supports(ex)}에서 true를 반환한 복수의 {@link SecurityExceptionHandler} 구현의 {@code supportedException()} 
	 * 		 메서드가 반환한 예외 {@link Class}들은 계층 구조 상에 부모-자식 관계를 이루기 때문이다.  
	 * 
	 * @param request 인증에 실패한 요청 객체.
	 * @param response 인증 실패 정보가 저장 될 응답 객체 
	 * @param exception 처리 될 exception 객체를 나타낸다.
	 *
	 * @throws IOException 응답 객체에 응답 객체 쓰기 연산 중 발생할 수 있다.
	 * @throws IllegalArgumentExcetpion {@code exceptionHandlers}의 엘리먼트들이 처리 불가능한 예외가 전달 될 경우 발생한다.
	 * @throws NullPointerException 
	 * 
	 */		
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request
									   , HttpServletResponse response
									   , AuthenticationException exception) throws IOException, ServletException {

		Objects.requireNonNull(exception);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Searching exception handler for {}", exception.toString());
		}
		
		SecurityErrorDTO errDTO = 
						exceptionHandlers.stream()
										 .filter(handler -> handler.supports(exception.getClass()))
										 .min((handler1, handler2) ->{
											 if(handler1.supportedException().isAssignableFrom(handler2.supportedException())) {
												return 1;
											 }else if(handler2.supportedException().isAssignableFrom(handler1.supportedException())){
												return -1;
											 }else{
												return 0;
											 }
										 })
										 .map(handler -> {
											 logger.debug("Selected exception handler : {}", handler);
											 return handler.handle(exception, request.getLocale());
										 })
										 .orElseThrow(() -> new IllegalArgumentException("Cannot resolve an exception argument"));
			
		logger.debug("Handling exception succeeded. Processing to build response...");
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(errDTO.getStatus());
		response.getWriter().write(objMapper.writeValueAsString(errDTO));
		
	}
	
	
	
	/**
	 * 
	 * seucurity 영역에서 발생한 예외를 처리할 {@link SecurityExceptionHandler}구현을 이 클래스의 {@code exceptionHandlers} 필드에 
	 * 추가한다. 메서드 내부에 형변환 코드가 존재하고 있는데 예를 들어 {@code AbstractSecurityExceptionHandler<AuthenticationServiceException>}를
	 * {@code AbstractSecurityExceptionHandler<AuthenticationException>} 타입으로 형변환해도 안전한데 그 이유는
	 * {@code AbstractSecurityExceptionHandler<AuthenticationServiceException>}의 메서드 파라미터 타입 
	 * {@code Class<? extends AuthenticationServiceException>}, 또는 {@code AuthenticationServiceException}을
	 * {@code AbstractSecurityExceptionHandler<Authentication>}의 메서드 파라미터 {@code Class<? extends AuthenticationException>} 또는 
	 * {@code AuthenticationException} 타입에 전달 가능하기 때문이다.
	 *  
	 * 
	 *
	 * @param exHandler - 인자 {@code exHandler}의 타입이 {@link AbstractSecurityExceptionHandler} 타입임을 알 수 있는데
	 * 					  이런 제약이 생긴 이유는 {@link AbstractSecurityExceptionHandler}의 주석에 설명되어 있다.
	 * 					  {@code exHandler} 오브젝트가 null일 경우 예외를 던지지 않고 단지 이를 추가 하지 않는다. 
	 *  
	 */
	public void addExceptionHandler(AbstractSecurityExceptionHandler<? extends AuthenticationException> exHandler){
		if(logger.isDebugEnabled()) {
			logger.debug("Received SecurityExceptionHandler : {}.", exHandler);
		}
		
		Optional.ofNullable(exHandler)
				.ifPresent(handler -> {
					@SuppressWarnings("unchecked")
					SecurityExceptionHandler<AuthenticationException> castedExHandler =
													(SecurityExceptionHandler<AuthenticationException>) exHandler;
					exceptionHandlers.add(castedExHandler);
					
					if(logger.isDebugEnabled()) {
						logger.debug("Adding SecurityExceptionHandler succeeded.");
					}
		});	
	}	
}
