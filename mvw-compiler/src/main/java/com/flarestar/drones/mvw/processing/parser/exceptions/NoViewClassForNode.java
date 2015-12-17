package com.flarestar.drones.mvw.processing.parser.exceptions;

public class NoViewClassForNode extends LayoutFileException {
    public NoViewClassForNode(String message) {
        super(message);
    }

    public NoViewClassForNode(String message, Throwable cause) {
        super(message, cause);
    }

    public NoViewClassForNode(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
