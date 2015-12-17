package com.flarestar.drones.mvw.processing.parser.exceptions;

/**
 * Created by runic on 10/10/15.
 */
public class InvalidStyleValue extends LayoutFileException {
    public InvalidStyleValue() {
        super();
    }

    public InvalidStyleValue(String message) {
        super(message);
    }

    public InvalidStyleValue(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidStyleValue(Throwable cause) {
        super(cause);
    }

    protected InvalidStyleValue(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
