package com.flarestar.drones.mvw.parser.exceptions;

/**
 * Created by runic on 11/13/15.
 */
public class MultipleViewClassesException extends LayoutFileException {
    public MultipleViewClassesException(String id, String viewClass, String otherViewClass) {
        super(createMessage(id, viewClass, otherViewClass));
    }

    public MultipleViewClassesException(String id, String viewClass, String otherViewClass, Throwable cause) {
        super(createMessage(id, viewClass, otherViewClass), cause);
    }

    public MultipleViewClassesException(String id, String viewClass, String otherViewClass, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(createMessage(id, viewClass, otherViewClass), cause, enableSuppression, writableStackTrace);
    }

    private static String createMessage(String id, String viewClass, String otherViewClass) {
        return "View #" + id + " has multiple directives w/ view classes, including (but not limited to): " + viewClass
            + " & " + otherViewClass + ". Each element must have only one associated android View class.";
    }
}
