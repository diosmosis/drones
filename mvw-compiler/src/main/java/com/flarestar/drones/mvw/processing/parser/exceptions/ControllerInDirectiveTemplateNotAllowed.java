package com.flarestar.drones.mvw.processing.parser.exceptions;

/**
 * TODO
 */
public class ControllerInDirectiveTemplateNotAllowed extends LayoutFileException {
    public ControllerInDirectiveTemplateNotAllowed(Class<?> directiveClass) {
        super(createMessage(directiveClass));
    }

    private static String createMessage(Class<?> directiveClass) {
        return "Use of ng-controller in " + directiveClass.getName() + "'s template is not allowed; use the "
            + "@DirectiveController annotation instead.";
    }
}
