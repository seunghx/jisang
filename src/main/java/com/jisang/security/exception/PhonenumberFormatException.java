package com.jisang.security.exception;

/**
 * 
 * 잘못 입력된 핸드폰 번호가 발견 되었을 때 이 예외가 던져진다. DB에 저장된 유저의 핸드폰 번호와 다른 핸드폰 번호가 전달되었을 때
 * 발생하는 {@link InvalidPhoneNumberException}과는 다르다.
 * 
 * 이 예외는 HTTP Status - 400 Bad Request에 해당되는 반면
 * {@link InvalidPhoneNumberException}은 HTTP Status - 401 - Unauthorized에 해당된다.
 * 
 * @author leeseunghyun
 *
 */
public class PhonenumberFormatException extends SecurityBadRequestException {

    // Static Fields
    // ==========================================================================================================================

    private static final long serialVersionUID = -1915410717145483986L;
    private static final String PHONE_NUMBER_FIELD = "phone";

    // Instance Fields
    // ==========================================================================================================================

    private String phoneNumber;

    // Constructors
    // ==========================================================================================================================

    public PhonenumberFormatException(String msg, String phoneNumber) {
        super(msg);
        this.phoneNumber = phoneNumber;
    }

    public PhonenumberFormatException(String msg, Throwable cause, String phoneNumber) {
        super(msg);
        initCause(cause);
        this.phoneNumber = phoneNumber;
    }

    // Methods 
    // ==========================================================================================================================

    public String getPhoneNumberField() {
        return PHONE_NUMBER_FIELD;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return super.toString() + "[malformed phoneNumber=" + phoneNumber + "]";
    }
}
