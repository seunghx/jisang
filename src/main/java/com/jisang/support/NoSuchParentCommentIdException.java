package com.jisang.support;

public class NoSuchParentCommentIdException extends RuntimeException {

    private static final long serialVersionUID = -359325216949008742L;

    private int invalidParentId;

    public NoSuchParentCommentIdException(String message, int invalidParentId) {
        super(message);
        this.invalidParentId = invalidParentId;

    }

    public int getInvalidParentId() {
        return invalidParentId;
    }

}
