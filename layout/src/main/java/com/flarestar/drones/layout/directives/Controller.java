package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.scope.Property;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveName("ng-controller")
@DirectiveMatcher(AttributeMatcher.class)
public class Controller extends Directive {
    public static class InvalidControllerAttribute extends LayoutFileException {
        public InvalidControllerAttribute(String message) {
            super(message);
        }
    }

    private final static Pattern controllerAttributeRegex = Pattern.compile("(#)?(\\w+)\\s+as\\s+(\\w+)");

    private String controllerClass;
    private String controllerScopeProperty;
    private boolean isInjected;

    private String initialValue;

    public Controller(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void manipulateViewNode(ViewNode node) throws LayoutFileException {
        super.manipulateViewNode(node);

        Matcher match = parseAttribute(node);

        isInjected = match.group(1) != null;
        controllerClass = match.group(2);
        controllerScopeProperty = match.group(3);

        context.addInjectedProperty(controllerClass, controllerScopeProperty);

        if (isInjected) {
            initialValue = context.getLayoutBuilderSimpleClassName() + ".this." + controllerScopeProperty;
        } else {
            initialValue = "new " + controllerClass + "(owner.getContext())";
        }

        properties.add(new Property(controllerScopeProperty, controllerClass, initialValue, this));
    }

    private Matcher parseAttribute(ViewNode node) throws InvalidControllerAttribute {
        String attributeValue = node.attributes.get("ng-controller");
        Matcher match = controllerAttributeRegex.matcher(attributeValue);
        if (!match.matches()) {
            throw new InvalidControllerAttribute("Invalid ng-controller attribute: " + attributeValue +
                ". Expected something like 'Controller as scopeProperty'.");
        }

        return match;
    }
}
