package com.jisang.security.exception;

/**
 * 
 * 잘못된 요청 파라미터가 발견 되었을 때 사용되는 예외 클래스이다. seuciry 단에서 잘못된 요청 파라미터가 발견되었을 경우 단지 인증
 * 실패 응답을 보내도 되나 클라이언트에 보다 더 자세한 응답을 보내기 위해 이 예외를 정의하였다.
 * 
 * @author leeseunghyun
 *
 */
public class BadRequestParameterDetectedException extends SecurityBadRequestException {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = 6473637003145014418L;
    
    // Instance Fields
    // ==========================================================================================================================

    private String paramName;
    private String paramValue;

    // Constructors
    // ==========================================================================================================================

    public BadRequestParameterDetectedException(String msg) {
        super(msg);
    }

    public BadRequestParameterDetectedException(String msg, String paramName, String paramValue) {
        this(msg);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    // Methods 
    // ==========================================================================================================================

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }
}
