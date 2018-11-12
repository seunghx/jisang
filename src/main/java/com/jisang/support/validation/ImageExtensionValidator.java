package com.jisang.support.validation;


import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jisang.support.ImageOperationProvider;
import com.jisang.support.UnsupportedImageFormatException;



/**
 * 
 * {@link ProductMutationDTO}의 {@link ImageExtension} 애노테이션이 붙은 {@link MultipartFile} 타입 프로퍼티에 대하여 올바른(지원하는) 이미지 확장자 인지를 
 * 검증한다.
 * 
 * @author leeseunghyun
 *
 */
@Component
public class ImageExtensionValidator implements ConstraintValidator<ImageExtension, MultipartFile>{
		
	private final Logger logger = LoggerFactory.getLogger(ImageExtensionValidator.class);
	
	@Autowired
	private MessageSource msgSource;
	
    @Override
    public void initialize(ImageExtension contactNumber) {
    }
   
	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
		
		if(Objects.isNull(file) || file.isEmpty()) {
			logger.info("Empty multipart file detected in {}.", this);
			
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					msgSource.getMessage("response.exception.multipart.empty", null, LocaleContextHolder.getLocale()))
					.addConstraintViolation();
			
			return false;
		}
		
		logger.debug("Delegating image file name validation to {}.", ImageOperationProvider.class);
		
		try {
			ImageOperationProvider.validateImage(file.getOriginalFilename());
		}catch(UnsupportedImageFormatException ex) {
			if(logger.isDebugEnabled()) {
				logger.debug("Validating image file name failed due to {}.", ex.toString());
				logger.debug("Invalid file name : {}", ex.getFileName());
			}
			return false;
		}
		
		logger.debug("Validating image file name succeeded.");
		return true;
		
	}

}
