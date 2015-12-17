package com.flarestar.drones.mvw.processing.parser.exceptions;

public class InvalidPropertyDescriptor extends LayoutFileException {
    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName) {
        super(createMessage(propertyDescriptor, directiveName));
    }

    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName, String message) {
        super(createMessage(propertyDescriptor, directiveName, message));
    }

    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName, Throwable cause) {
        super(createMessage(propertyDescriptor, directiveName), cause);
    }

    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
        super(createMessage(propertyDescriptor, directiveName), cause, enableSuppression, writableStackTrace);
    }

    private static String createMessage(String propertyDescriptor, String directiveName) {
        return createMessage(propertyDescriptor, directiveName, null);
    }

    private static String createMessage(String propertyDescriptor, String directiveName, String reason) {
        String result = "Invalid property descriptor '" + propertyDescriptor + "' in directive '" + directiveName + "'";
        if (reason == null) {
            result += ".";
        } else {
            result += ": " + reason;
        }
        return result;
    }
}
