package com.flarestar.drones.mvw.compilerutilities.exceptions;

public class BaseExpressionException extends Exception {
    public BaseExpressionException() {
    }

    public BaseExpressionException(String message) {
        super(message);
    }

    public BaseExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseExpressionException(Throwable cause) {
        super(cause);
    }

    public BaseExpressionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
