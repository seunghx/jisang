package com.jisang.support.validation;

import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.jisang.dto.product.ProductListViewConfigData;

/**
 * 
 * {@link ProductListViewConfigData} 클래스 오브젝트에 대한 프로퍼티 연관 관계 검증을 위해 사용될 validation 애노테이션이다.
 * 
 * 
 * @author leeseunghyun
 *
 */

@Constraint(validatedBy = ProductListViewValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProductListViewConstraint {
	String message() default "{ProductListViewConstraint.defaultMessage}";
	
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    
	String viewType();
    String[] viewTypeArguments();
}
