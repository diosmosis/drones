package com.flarestar.drones.mvw.function.exceptions;

/**
 * TODO
 */
public class InvalidUserFunctionClass extends Exception {
    public InvalidUserFunctionClass() {
    }

    public InvalidUserFunctionClass(String message) {
        super(message);
    }

    public InvalidUserFunctionClass(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUserFunctionClass(Throwable cause) {
        super(cause);
    }

    public InvalidUserFunctionClass(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
