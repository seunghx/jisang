package com.jisang.service.product;

/**
 * 
 * 타 마켓 관리자 계정으로 부터 해당 관리자가 관리하지 않는 상품에 대한 수정 요청이 발견되었을 경우 아래 예외가 던져진다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class IllegalManagerAccessDetectedException extends RuntimeException {

    private static final long serialVersionUID = 9214203336725339712L;

    private int managerId;
    private int productId;

    public IllegalManagerAccessDetectedException(String message) {
        super(message);
    }

    public IllegalManagerAccessDetectedException(String message, Throwable cause) {
        this(message);
        initCause(cause);
    }

    public IllegalManagerAccessDetectedException(String message, int managerId, int productId) {
        this.managerId = managerId;
        this.productId = productId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getManagerId() {
        return managerId;
    }

    public int getProductId() {
        return productId;
    }

}
