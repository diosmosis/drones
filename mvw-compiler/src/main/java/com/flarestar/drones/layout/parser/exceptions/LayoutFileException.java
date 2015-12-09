package com.flarestar.drones.layout.parser.exceptions;

/**
 * Created by runic on 9/24/15.
 */
public class LayoutFileException extends Exception {
    public LayoutFileException() {
        super();
    }

    public LayoutFileException(String message) {
        super(message);
    }

    public LayoutFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public LayoutFileException(Throwable cause) {
        super(cause);
    }

    protected LayoutFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
