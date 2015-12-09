package com.flarestar.drones.layout.parser.exceptions;

public class MissingLayoutAttributeValue extends LayoutFileException {
    public MissingLayoutAttributeValue(String attributeName) {
        super(makeMessage(attributeName));
    }

    public MissingLayoutAttributeValue(String attributeName, Throwable cause) {
        super(makeMessage(attributeName), cause);
    }

    public MissingLayoutAttributeValue(String attributeName, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(makeMessage(attributeName), cause, enableSuppression, writableStackTrace);
    }

    private static String makeMessage(String attributeName) {
        return "Attribute '" + attributeName + "' is required!";
    }
}
