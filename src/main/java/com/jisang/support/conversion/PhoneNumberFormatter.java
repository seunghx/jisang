package com.jisang.support.conversion;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.Formatter;


public class PhoneNumberFormatter implements Formatter<String>{

	private static final String TOPIC_NAME_FOR_UNSUPPORTED_LOCALE_EXCEPTION = "phone";
	
	private Logger logger = LoggerFactory.getLogger(PhoneNumberFormatter.class);
	
	private final List<PhoneNumberFormatProvider> formatters;
	
	public PhoneNumberFormatter(List<PhoneNumberFormatProvider> formatters) {
		this.formatters = formatters;
		
		if(Objects.isNull(this.formatters) || this.formatters.isEmpty()) {
			if(logger.isInfoEnabled()) {
				logger.info("Received invalid argument. Argument formatters : {}", formatters);
			}
			throw new IllegalArgumentException("Argument formatters is null or empty list.");
		}
	}
	
	@Override
	public String print(String formattedPhoneNumber, Locale locale) {
		
		return formatters.stream()
				 	     .filter(formatter -> formatter.supports(locale))
				 	     .findFirst()
				 	     .map(formatter -> formatter.print(formattedPhoneNumber))
				 	     .orElseThrow(() ->{
				 	    	 if(logger.isInfoEnabled()) {
				 	    		 logger.info("Received unsupported Locale argument. Argument locale : {}.", locale);
				 	    	 }
				 	    	 throw new UnsupportedLocaleException("Unsupported Locale detected.", TOPIC_NAME_FOR_UNSUPPORTED_LOCALE_EXCEPTION);
				 	     });
	}

	@Override
	public String parse(String rawPhoneNumber, Locale locale) throws ParseException {
		
		return formatters.stream()
						 .filter(formatter -> formatter.supports(locale))
						 .findFirst()
						 .map(formatter -> formatter.parse(rawPhoneNumber))
						 .orElseThrow(() ->{
							 if(logger.isInfoEnabled()) {
								 logger.info("Received unsupported Locale argument. Argument locale : {}.", locale);
							 }
							 throw new UnsupportedLocaleException("Unsupported Locale detected.", TOPIC_NAME_FOR_UNSUPPORTED_LOCALE_EXCEPTION);
						 });
	}

}
