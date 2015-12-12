package com.flarestar.drones.mvw.parser.exceptions;

/**
 * TODO
 */
public class MissingRequiredAttribute extends LayoutFileException {
    public MissingRequiredAttribute() {
    }

    public MissingRequiredAttribute(String message) {
        super(message);
    }

    public MissingRequiredAttribute(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRequiredAttribute(Throwable cause) {
        super(cause);
    }

    public MissingRequiredAttribute(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
