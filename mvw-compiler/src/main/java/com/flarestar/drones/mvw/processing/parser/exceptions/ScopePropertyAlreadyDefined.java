package com.flarestar.drones.mvw.processing.parser.exceptions;

public class ScopePropertyAlreadyDefined extends RuntimeException {
    public ScopePropertyAlreadyDefined(String property, String definedDirective) {
        super(createMessage(property, definedDirective));
    }

    private static String createMessage(String property, String definedDirective) {
        return "Attempting to define scope property twice: '" + property + "' first defined by '" + definedDirective
            + "'.";
    }
}
