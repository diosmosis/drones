package com.flarestar.drones.layout.parser.exceptions;

/**
 * Created by runic on 9/24/15.
 */
public class InvalidLayoutAttributeValue extends LayoutFileException {
    public InvalidLayoutAttributeValue() {
    }

    public InvalidLayoutAttributeValue(String message) {
        super(message);
    }

    public InvalidLayoutAttributeValue(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLayoutAttributeValue(Throwable cause) {
        super(cause);
    }

    public InvalidLayoutAttributeValue(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
