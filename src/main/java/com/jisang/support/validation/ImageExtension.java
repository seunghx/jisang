package com.jisang.support.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 
 * {@link MultipartFile} 타입 프로퍼티의 확장자에 대한 bean validation 수행을 위한 애노테이션.
 * 
 * @author leeseunghyun
 *
 */
@Documented
@Constraint(validatedBy = ImageExtensionValidator.class)
@Target({ ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageExtension {

    String message() default "Unsupported image format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
