package com.jisang.support.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


@Documented
@Constraint(validatedBy = MallLocationValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExistingMallLocation {
	
	String message() default "Invalid location info";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {}; 
}
