package com.flarestar.drones.mvw.processing.parser.exceptions;

/**
 * Created by runic on 11/6/15.
 */
public class MultipleIsolateScopesException extends LayoutFileException {
    public MultipleIsolateScopesException(String newDirectiveName, String existingDirectiveName) {
        super(makeMessage(newDirectiveName, existingDirectiveName));
    }

    public MultipleIsolateScopesException(String newDirectiveName, String existingDirectiveName, Throwable cause) {
        super(makeMessage(newDirectiveName, existingDirectiveName), cause);
    }

    public MultipleIsolateScopesException(String newDirectiveName, String existingDirectiveName, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(makeMessage(newDirectiveName, existingDirectiveName), cause, enableSuppression, writableStackTrace);
    }

    private static String makeMessage(String newDirectiveName, String existingDirectiveName) {
        return "Multiple directives w/ isolate scopes used on node. Existing = '" + existingDirectiveName
            + "', trying to add = '" + newDirectiveName + "'.";
    }
}
