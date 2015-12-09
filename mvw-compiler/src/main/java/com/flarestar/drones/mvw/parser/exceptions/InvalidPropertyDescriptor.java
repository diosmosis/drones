package com.flarestar.drones.mvw.parser.exceptions;

public class InvalidPropertyDescriptor extends LayoutFileException {
    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName) {
        super(createMessage(propertyDescriptor, directiveName));
    }

    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName, Throwable cause) {
        super(createMessage(propertyDescriptor, directiveName), cause);
    }

    public InvalidPropertyDescriptor(String propertyDescriptor, String directiveName, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
        super(createMessage(propertyDescriptor, directiveName), cause, enableSuppression, writableStackTrace);
    }

    private static String createMessage(String propertyDescriptor, String directiveName) {
        return "Invalid property descriptor '" + propertyDescriptor + "' in directive '" + directiveName + "'.";
    }
}
