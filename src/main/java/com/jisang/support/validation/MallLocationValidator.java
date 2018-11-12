package com.jisang.support.validation;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jisang.config.code.CodeBook.MallLocation;


public class MallLocationValidator implements ConstraintValidator<ExistingMallLocation, String> {

	private static final Logger logger = LoggerFactory.getLogger(MallLocationValidator.class);
	
	@Override
	public boolean isValid(String mallLocation, ConstraintValidatorContext context) {
		
		logger.debug("Starting to validate {}", mallLocation);
		
		return Optional.ofNullable(mallLocation)
					   .filter(mallLoc -> MallLocation.fromString(mallLoc) != null)
					   .isPresent();
	}

}
