package com.flarestar.drones.layout.parser.exceptions;

/**
 * Created by runic on 9/24/15.
 */
public class UnknownLayoutAttributeException extends LayoutFileException {
    public UnknownLayoutAttributeException(String attributeName) {
        super("Unknown layout attribute '" + attributeName + "'.");
    }
}
