package com.jisang.security.exception.handler;

import java.util.Locale;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import com.jisang.security.dto.SecurityErrorDTO;
import com.jisang.security.exception.BadRequestParameterDetectedException;
import com.jisang.security.exception.SecurityBadRequestException;
import com.jisang.security.exception.SecurityUnsupportedLocaleException;
import com.jisang.security.exception.PhonenumberFormatException;

/**
 * 
 * 400 - Bad Request 응답 처리에 해당하는 예외를 처리하는 {@link SecurityExceptionHandler} 구현이다.
 * 
 * @author leeseunghyun
 *
 */
public class BadRequestExceptionHandler extends AbstractSecurityExceptionHandler<SecurityBadRequestException> {

    // Static Fields
    // ==========================================================================================================================

    /**
     * 이 {@link SecurityExceptionHandler} 구현이 처리하는 예외에 해당하는 HTTP status 코드.
     */
    private static final int HTTP_STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST.value();

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public BadRequestExceptionHandler(MessageSource msgSource) {
        super(SecurityBadRequestException.class, msgSource);
    }

    // Methods
    // ==========================================================================================================================

    @Override
    public SecurityErrorDTO handle(SecurityBadRequestException e, Locale locale) {
        Objects.requireNonNull(e);

        logger.error("Exception to be handled :", e);

        SecurityErrorDTO dto = new SecurityErrorDTO(HTTP_STATUS_BAD_REQUEST);

        if (e instanceof PhonenumberFormatException) {
            String phoneFieldName = ((PhonenumberFormatException) e).getPhoneNumberField();
            String phoneNumber = ((PhonenumberFormatException) e).getPhoneNumber();

            dto.addDetail(phoneFieldName, phoneNumber);
            dto.setMessage(
                    msgSource.getMessage("response.exception.response.exception.InvalidPhonenumberFormatException",
                            null, "Invalid phone number.", locale));
        } else if (e instanceof BadRequestParameterDetectedException) {
            String paramName = ((BadRequestParameterDetectedException) e).getParamName();
            String paramValue = ((BadRequestParameterDetectedException) e).getParamValue();

            dto.addDetail(paramName, paramValue);

            dto.setMessage(msgSource.getMessage("response.exception.BadRequestParameterDetectedException", null,
                    "Missing parameters : " + paramName, locale));
        } else if (e instanceof SecurityUnsupportedLocaleException) {
            String localeField = ((SecurityUnsupportedLocaleException) e).getLocaleField();
            Locale unsupportedLocale = ((SecurityUnsupportedLocaleException) e).getLocale();

            dto.addDetail(localeField, unsupportedLocale.toString());
            dto.setMessage(msgSource.getMessage("response.exception.UnsupportedLocaleException.", null,
                    "Unsupported Locale.", locale));
        }

        return dto;
    }

}
