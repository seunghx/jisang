package com.jisang.support;

public class NoSuchProductException extends RuntimeException {

	private static final long serialVersionUID = -4420234643167096697L;
	
	private int productId;
	
	public NoSuchProductException(String message, int productId) {
		super(message);
		this.productId = productId;
	}
	
	public NoSuchProductException(String message, Throwable cause, int productId) {
		this(message, productId);
		initCause(cause);
	}

	public int getProductId() {
		return productId;
	}
}
