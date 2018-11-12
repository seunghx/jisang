package com.jisang.support.validation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

/**
 * 
 * {@link Validator}의 메서드를 호출하여 bean validation을 수행한다. 검증 작업 중 예외가 발생하면 이에 대한 로깅으로 어느 필드에 어느 문제가 있는지 
 * 알아야할 필요가 있는데, 빈 검증 작업을 호출하는 곳의 코드가 이러한 로깅때문에 더렵혀지는 것을 방지하고 또한 코드의 중복을 완화시켜주기 위해 이 클래스를 작성하게
 * 되었다. 
 * 
 * @author leeseunghyun
 *
 */
@Component
public class ValidationDelegator {
	
	private final Logger logger = LoggerFactory.getLogger(ValidationDelegator.class);
	
	@Autowired
	protected Validator validator;
	
	public <T> void validate(T obj, Class<?>... groups) {
		
		logger.debug("Starting to validate object...");
		
		Objects.requireNonNull(obj, "Null value argument obj detected while trying to validate object.");
		
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj, groups);
		Set<FieldError> fieldErrors = new HashSet<>();
		
		if(constraintViolations.size() > 0) {
			logger.error("Argument is invalid. Argument : {}.", obj);
			
			for(Class<?> group : groups) 
				logger.info("Validation groups : {}", group);
				
			for(ConstraintViolation<T> violation : constraintViolations) {
				if(logger.isInfoEnabled()) {
					logger.info("Violation occurred at {}", violation.getPropertyPath());
					logger.info("Constraint message = {}",  violation.getMessage());
					logger.info("Annotation type = {}", violation.getConstraintDescriptor().getAnnotation().annotationType());
					logger.info("Detected invalid value = {}", violation.getInvalidValue());

					fieldErrors.add(new FieldError(obj.getClass().getSimpleName(), violation.getPropertyPath().toString(), violation.getMessage()));
				}
			}
			
			throw new FieldErrorBasedValidationException("Invalid object detected. Invalid object :" + obj, fieldErrors);
		}
		
		logger.debug("validation succeeded. Validated object : {}", obj);
	}	
}


