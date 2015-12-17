package com.flarestar.drones.mvw.processing.parser.directive.exceptions;

public class InvalidDirectiveClassException extends RuntimeException {
    public InvalidDirectiveClassException(String message) {
        super(message);
    }

    public InvalidDirectiveClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDirectiveClassException(Throwable cause) {
        super(cause);
    }

    public InvalidDirectiveClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
