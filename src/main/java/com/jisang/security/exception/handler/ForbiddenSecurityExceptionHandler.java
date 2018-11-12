package com.jisang.security.exception.handler;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import com.jisang.security.dto.SecurityErrorDTO;

/**
 * 
 * spring security의 {@link AccessDeniedException}에 대한
 * {@link SecurityExceptionHandler} 구현이다.
 * 
 * @author leeseunghyun
 *
 */
public class ForbiddenSecurityExceptionHandler extends AbstractSecurityExceptionHandler<AccessDeniedException> {

    // Static Fields
    // ==========================================================================================================================

    /**
     * 이 {@link SecurityExceptionHandler} 구현이 처리하는 예외에 해당하는 HTTP status 코드.
     */
    private static final int HTTP_STATUS_FORBIDDEN = HttpStatus.FORBIDDEN.value();

    // Instance Fields
    // ==========================================================================================================================

    // Constructors
    // ==========================================================================================================================

    public ForbiddenSecurityExceptionHandler(MessageSource msgSource) {
        super(AccessDeniedException.class, msgSource);
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 이 메서드 구현은 단순히 HTTP 응답 403 - Forbidden을 나타내는 {@link SimpleResponseDTO}를 반환한다.
     * 반환 되는 DTO는 항상 같은 응답 status code와 message를 갖고 있기 때문에 반환용
     * {@link SimpleResponseDTO}를 프로퍼티로 갖고 이를 반환하는 방법도 있겠지만, 이럴 경우 외부에서 반환된 DTO의 내부를
     * 바꿀 위험이 있기 때문에 이 메서드에서 반환할 객체를 매번 생성해서 반환하는 방법을 택하였다.
     * 
     */
    @Override
    public SecurityErrorDTO handle(AccessDeniedException e, Locale locale) {

        if (logger.isDebugEnabled()) {
            logger.debug("Handling AccessDeniedException...");
        }
        logger.error("AccessDeniedException to be handled :", e);

        SecurityErrorDTO dto = new SecurityErrorDTO(HTTP_STATUS_FORBIDDEN);
        dto.setMessage(msgSource.getMessage("http.response.Forbidden", null, "A request is Forbidden", locale));
        return dto;
    }

}
