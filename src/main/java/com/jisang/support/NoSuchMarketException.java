package com.jisang.support;

public class NoSuchMarketException extends RuntimeException {

    private static final long serialVersionUID = 1444834809249883842L;

    private int marketId;

    public NoSuchMarketException(String message, int marketId) {
        super(message);
        this.marketId = marketId;
    }

    public NoSuchMarketException(String message, Throwable cause, int marketId) {
        super(message);
        initCause(cause);

        this.marketId = marketId;
    }

    public int getMarketId() {
        return marketId;
    }
}
