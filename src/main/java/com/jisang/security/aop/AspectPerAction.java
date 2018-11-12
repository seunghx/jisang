package com.jisang.security.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import com.jisang.security.dto.TokenDTO;
import com.jisang.security.exception.AuthenticationNumberExpiredException;
import com.jisang.security.exception.JWTAuthenticationException;


/**
 * 
 * com.jisang.security 하위의 클래스 중 특정 동작을 취하는 클래스의 메서드에 적용될 Aspect 구현 클래스이다. 
 * 여기서 말하는 특정 동작이란 각각의 메서드 단위가 아닌 인증 작업, 혹은 JWT token building 작업 등 하나 이상의 메서드의 콜래보레이션 
 * 수준의 동작을 말한다. 
 * 
 * 인자에 대한 null 검사는 오브젝트를 사용하는 쪽에서 하는 것이 가장 안전하다는 생각에 생략하였다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Order(2)
@Component
public class AspectPerAction {
	
	
	// Static Fields
	//==========================================================================================================================
	
	
	// Instance Fields
	//==========================================================================================================================
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// Constructors
	//==========================================================================================================================
	

	// Methods
	//==========================================================================================================================

	/**
	 * 
	 * 인증 작업은 하나의 큰 작업이라고 할 수 있다. 이런 작업의 시작과 끝을 알리기 위해 작성된 로깅 애스팩트이다. 
	 * 하나의 (하나의 메서드 호출보다는)큰 작업 흐름에 대한 로깅이기 때문에 로깅 레벨은 INFO로 정하였다.
	 *
	 */
	@Around("execution(* com.jisang.security.authentication.provider.*AuthenticationProvider.authenticate(..))")
	public Authentication authProviderAuthenticateLogging(ProceedingJoinPoint jp) throws Throwable {
		logger.info("Processing Authentication...");
		
		try {
			Object result = jp.proceed();
			
			logger.info("Authentication process succeeded.");
			
			return (Authentication)result;
		}catch(DataAccessException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Authentication process failed.");
				logger.info("#{} throws {}.", jp.getSignature(), e.toString());
				logger.info("Converting {} to AuthenticationServiceException...", e.toString());	
			}
			throw new AuthenticationServiceException(e.toString(), e);
		}catch(Exception e) {
			logger.info("Authentication process failed.");
			
			throw e;
		}
	}
	
	/**
	 * 
	 * JWT token parsing 작업의 시작과 끝을 알리기 위해 작성된 로깅 애스팩트이다. 사실 JWT token parsing은 인증 작업의 한 과정일 뿐이라고 볼 수도 있으나
	 * JWT token 인증 기반으로 동작하는 어플리케이션이기 때문에 기타 메서드 호출들보다는 중요하다고 생각하여 {@link AbstractJWTService#parseToken} 메서드에 대한 
	 * INFO 레벨 로깅 애스팩트를 추가하게 되었다.
	 * 
	 * 예외 처리 부분을 보면 대부분의 예외를 {@link Insufficient
	 * 
	 */
	@Around("execution(* com.jisang.security.service.*JWTService.parseToken(..))")
	public TokenDTO parseJWTTokenExceptionTranslationAndLogging(ProceedingJoinPoint jp) throws Throwable {
		logger.info("Parsing JWT token...");
		
		try {
			Object result = jp.proceed();
			
			logger.info("Parsing JWT token succeeded.");
			
			return (TokenDTO)result;
		}catch(IllegalArgumentException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws IllegalArgumentException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to InternalAuthenticationServiceException...", e.toString());
			}
			throw new InternalAuthenticationServiceException(e.toString(), e);
		}catch(UnsupportedJwtException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws UnsupportedJwtException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to JWTAuthenticationException...", e.toString());
				logger.warn("Invalid JWT token detected!!!!. This JWT token is not ours.");
			}
			throw new JWTAuthenticationException("Invalid JWT token detected.", e);
		}catch(ExpiredJwtException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws ExpiredJwtException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to CredentialsExpiredException...", e.toString());
			}
			throw new CredentialsExpiredException("JWT token expired", e);
		}catch(MalformedJwtException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws MalformedJwtException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to JWTAuthenticationException...", e.toString());
			}
			logger.warn("Invalid JWT token detected!.");
			throw new JWTAuthenticationException("Invalid JWT token detected.", e);
		}catch(IncorrectClaimException e) {
			logger.warn("Invalid JWT token detected!. Claims have unexpected value.");
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws IncorrectClaimException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to JWTAuthenticationException...", e.toString());
			}
			throw new JWTAuthenticationException("Invalid JWT token detected. Required claim is incorrect", e);
		}catch(MissingClaimException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws MissingClaimException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to JWTAuthenticationException...", e.toString());
			}
			logger.warn("Invalid JWT token detected. Required claim doesn't exist.");
			throw new JWTAuthenticationException("Invalid JWT token detected", e);
		}catch(JwtException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Parsing JWT token failed.");
				logger.info("{}#{} throws JwtException.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting {} to InternalAuthenticationServiceException...", e.toString());
			}
			throw new InternalAuthenticationServiceException(e.toString(), e);
		}catch(AuthenticationNumberExpiredException e) {
			throw e;
		}
		catch(Exception e) {
			logger.info("Parsing JWT token failed.");
			throw new InternalAuthenticationServiceException("An System exception occurred while trying to parse JWT.", e);
		}
	}
	
	/**
	 * 
	 * JWT token building 작업의 시작과 끝을 알리기 위해 작성된 로깅 애스팩트이다. 사실 JWT token building은 인증 작업의 한 과정일 뿐이라고 볼 수도 있으나
	 * JWT token 인증 기반으로 동작하는 어플리케이션이기 때문에 기타 메서드 호출들보다는 중요하다고 생각하여 {@link AbstractJWTService#buildToken} 메서드에 대한 
	 * INFO 레벨 로깅 애스팩트를 추가하게 되었다. 
	 * 
	 *
	 */
	@Around("execution(* com.jisang.security.service.*JWTService.buildToken(..))")
	public String buildJWTTokenExceptionTranslationAndLogging(ProceedingJoinPoint jp) throws Throwable {
		if(logger.isInfoEnabled()) {
			logger.info("Building JWT token...");
		}
		try {
			Object result = jp.proceed();
			
			logger.info("Building JWT succeeded.");
			
			return (String)result;
			
		}catch(SignatureException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Building JWT failed.");
				logger.info("{}#{} throws SignatureException while trying to set SignatureAlgorithm.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting SignatureException to InternalAuthenticationServiceException...");
			}
			
			throw new InternalAuthenticationServiceException("Invalid signature algorithm property.");
		}catch(CompressionException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Building JWT failed.");
				logger.info("{}#{} throws CompressionException  while trying to compress JWT token.", jp.getTarget(), jp.getSignature().toShortString());
				logger.info("Converting CompressionException to InternalAuthenticationServiceExcetpion...");
			}
			
			throw new InternalAuthenticationServiceException("JWT token compression failed.");
		}catch(Exception e) {
			logger.info("Building JWT failed.");
			throw new InternalAuthenticationServiceException("An System exception occurred while trying to build JWT.", e);
		}
	}
	
	
	/**
	 * 
	 * 클라이언트에 인증 번호를 전송하는 {@link AuthenticationNumberNotificationProvider} 구현 클래스에 적용될 로깅 애스팩트.
	 *
	 */
	@Around("execution(* com.jisang.security.support.AuthenticationNumberNotificationProvider+.sendAuthenticationNumber(..))")
	public Object authenticationNumberNotificationLogging(ProceedingJoinPoint jp) throws Throwable {
		
		logger.info("Starting sending SMS to requested user.");
		
		
		try {
			Object result = jp.proceed();
			
			logger.info("Sending SMS to receiver succeeded.");
			
			return result;
		}catch(RuntimeException e) {
			logger.info("Sending SMS to receiver failed.");
			
			throw e;
		}
	}
	
	/**
	 * 
	 * jisang 어플리케이션의 security 단에서의 인증 과정 중 data access layer에서 발생한 {@link DataAccessException} 타입 예외를 
	 * {@link AuthenticationFailureHandler} 등에서 처리할 수 있도록 {@link AuthenticationServiceException}으로 변환시킨다.
	 * 
	 */
	@AfterThrowing(value="execution(* com.jisang.security.persistence.*.*(..))", throwing = "ex")
	public void authenticationProccessingDataAccessExceptionTranslation(JoinPoint jp, DataAccessException ex) throws RuntimeException {
		
		if(logger.isInfoEnabled()) {
			logger.info("Exception occurred in Data Acess Layer - {}#{}", jp.getTarget(), jp.getSignature().toShortString());
			logger.info("Converting {} to AuthenticationServiceExceptoin. ", ex.toString());
		}
		
		throw new AuthenticationServiceException(ex.toString(), ex);
	}
}
