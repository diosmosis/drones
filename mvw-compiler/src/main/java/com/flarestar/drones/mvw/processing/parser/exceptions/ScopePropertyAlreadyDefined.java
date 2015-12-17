package com.flarestar.drones.mvw.processing.parser.exceptions;

public class ScopePropertyAlreadyDefined extends LayoutFileException {
    public ScopePropertyAlreadyDefined(String property, String definedDirective, String secondDirective) {
        super(createMessage(property, definedDirective, secondDirective));
    }

    public ScopePropertyAlreadyDefined(String property, String definedDirective, String secondDirective,
                                       Throwable cause) {
        super(createMessage(property, definedDirective, secondDirective), cause);
    }

    public ScopePropertyAlreadyDefined(String property, String definedDirective, String secondDirective,
                                       Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(createMessage(property, definedDirective, secondDirective), cause, enableSuppression, writableStackTrace);
    }

    private static String createMessage(String property, String definedDirective, String secondDirective) {
        return "Attempting to define scope property twice: '" + property + "' first defined by '" + definedDirective
            + "', then defined by '" + secondDirective + "'.";
    }
}
