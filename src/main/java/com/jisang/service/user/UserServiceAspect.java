package com.jisang.service.user;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
 * {@link UserService} 구현 클래스에 대한 애스팩트를 정의한 클래스이다.
 * 
 * 현재 구현은 {@link UserService} 구현 클래스의 메서드의 시작과 끝을 알리는 로깅만이 정의되어 있다. 
 * 각각의 서비스 오브젝트의 메서드는 각각의 비즈니스 로직을 의미하므로 비즈니스 로직의 시작과 끝을 알린다는 의미로 info 레벨의 로깅을 수행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Component
@Order(2)
public class UserServiceAspect {
	private final Logger logger = LoggerFactory.getLogger(UserServiceAspect.class);
	
	/**
	 * {@link UserService#registerUser(com.jisang.dto.SignupDTO)} 구현에 적용될 로깅 애스팩트이다. 
	 */
	@Around("execution(* com.jisang.service.user.*.registerUser(..))")
	public Object registrationUserLog(ProceedingJoinPoint jp) throws Throwable {
		if(logger.isInfoEnabled()) {
			logger.info("Starting new user registration by {}", jp.getTarget());
		}
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("New user registration succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("New user registration failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.user.*.modifyUser(..))")
	public Object modifyUserLog(ProceedingJoinPoint jp) throws Throwable {
		if(logger.isInfoEnabled()) {
			logger.info("Starting user info modification by {}", jp.getTarget());
		}
		try {
			Object result = jp.proceed();

			logger.info("User info modification succeeded.");
			
			
			return result;
		}catch(Exception e) {
			logger.info("User info modification failed.");
			
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.user.*.find*(..))")
	public Object findUserLog(ProceedingJoinPoint jp) throws Throwable {
		if(logger.isInfoEnabled()) {
			logger.info("Starting to get user info  by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();
			
			logger.info("Getting user info succeeded.");
			
			return result;
		}catch(Exception e) {
			logger.info("Getting user info failed.");
			
			throw e;
		}
	}
}
