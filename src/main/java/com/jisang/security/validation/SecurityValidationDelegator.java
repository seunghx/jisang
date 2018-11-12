package com.jisang.security.validation;

import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;



/**
 * 
 * security단의 오브젝트에 대한 bean validation을 수행하는 클래스이다. 
 * 
 * com.jisang.security와 그 하위의 패키지 컴포넌트들을 후에 다시 사용할 일이 있을 경우를 대비하여 com.jisang.security 외부와 최대한 분리시키려는 개인적인
 * 목적 때문에 com.jisang.validation에도 {@link ValidationDelegator} 클래스가 있으나 이 클래스를 사용하지 않았다. 또한 {@link ValidationDelegator} 
 * 클래스와 다른 점이라곤 던져지는 예외에 타입 하나뿐인데, {@link ValidationDelegator}를 계승하여 오버라이딩하지 않은 이유도 위의 설명한 목적 때문이다.
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
@Component
public class SecurityValidationDelegator {
	private final Logger logger = LoggerFactory.getLogger(SecurityValidationDelegator.class);
	
	@Autowired
	protected Validator validator;
	
	public <T> void validate(T obj, Class<?>... groups) {
		Optional.ofNullable(obj).orElseThrow(() -> new InternalAuthenticationServiceException("Argument obj is null."));
		
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj, groups);
		
		if(constraintViolations.size() > 0) {
			logger.error("Validation failed. validating object : {}.", obj);
			
			if(logger.isInfoEnabled()) {
				for(Class<?> group : groups) 
					logger.info("Validation groups : {}", group);
				
				for(ConstraintViolation<T> violation : constraintViolations) {
					logger.info("Violation occurred at {}", violation.getPropertyPath());
					logger.info("Constraint message = {}",  violation.getMessage());
					logger.info("Annotation type = {}", violation.getConstraintDescriptor().getAnnotation().annotationType());
					logger.info("Detected invalid value = {}", violation.getInvalidValue());
				}
			}
			throw new InternalAuthenticationServiceException("Invalid object detected. Invalid object :" + obj);
		}	
	}
	
}
