package com.jisang.support.conversion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;


public class PhoneNumberFormatterFactory implements AnnotationFormatterFactory<PhoneNumberFormat>{

	@Autowired
	@Qualifier("phoneNumberFormatter") 
	private Formatter<String> phoneNumberFormatter;
	
	@Override
	public Set<Class<?>> getFieldTypes() {
		return new HashSet<>(Arrays.asList(String.class));
	}

	@Override
	public Printer<String> getPrinter(PhoneNumberFormat annotation, Class<?> fieldType) {
		return phoneNumberFormatter;
	}

	@Override
	public Parser<String> getParser(PhoneNumberFormat annotation, Class<?> fieldType) {
		return phoneNumberFormatter;
	}

}
