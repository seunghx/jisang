package com.jisang.support.conversion;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class KoreaPhoneNumberFormatProvider implements PhoneNumberFormatProvider {

    private final Logger logger = LoggerFactory.getLogger(KoreaPhoneNumberFormatProvider.class);

    private static final String COUNTRY_CODE = "+82";

    @Override
    public String parse(String rawPhoneNumber) {
        logger.debug("Starting to parse row phone number...");

        if (StringUtils.isEmpty(rawPhoneNumber)) {
            if (logger.isInfoEnabled()) {
                logger.info("Argument rawPhoneNumber must not be empty. Argument rawPhoneNumber: {}", rawPhoneNumber);
            }
            throw new IllegalArgumentException("Argument rawPhoneNumber is empty.");
        }

        StringBuilder builder = new StringBuilder(rawPhoneNumber.trim());
        builder.replace(0, 1, COUNTRY_CODE);
        builder.insert(5, "-");
        builder.insert(builder.length() - 4, "-");

        return builder.toString();

    }

    @Override
    public String print(String formattedPhoneNumber) {
        logger.debug("Starting to print formattedPhoneNumber...");

        if (StringUtils.isEmpty(formattedPhoneNumber)) {
            if (logger.isInfoEnabled()) {
                logger.info("Argument rawPhoneNumber must not be empty.Argument formattedPhoneNumber: {}",
                        formattedPhoneNumber);
            }
            throw new IllegalArgumentException("Argument formattedPhoneNumber is empty.");
        }

        StringBuilder builder = new StringBuilder(formattedPhoneNumber.replaceAll("-", ""));
        builder.replace(0, 3, "0");

        return builder.toString();
    }

    @Override
    public boolean supports(Locale locale) {
        return Locale.KOREAN.equals(locale) || Locale.KOREA.equals(locale);
    }

}
