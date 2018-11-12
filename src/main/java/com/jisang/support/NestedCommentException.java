package com.jisang.support;

/**
 * 
 * 현재 지상 어플리케이션에서 허용하는 댓글 깊이(댓글에 대한 댓글)를 초과한 깊이의 댓글이 발견될 경우 아래 예외가 던져진다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class NestedCommentException extends RuntimeException {

	private static final long serialVersionUID = -7229378501620017258L;

	public NestedCommentException(String message) {
		super(message);
	}
	
	public NestedCommentException(String message, Throwable cause) {
		this(message);
		initCause(cause);
	}

}
