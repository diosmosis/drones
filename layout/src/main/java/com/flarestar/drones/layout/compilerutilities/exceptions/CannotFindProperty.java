package com.flarestar.drones.layout.compilerutilities.exceptions;

public class CannotFindProperty extends BaseExpressionException {
    public CannotFindProperty(String message) {
        super(message);
    }

    public CannotFindProperty(String message, Throwable cause) {
        super(message, cause);
    }

    protected CannotFindProperty(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
