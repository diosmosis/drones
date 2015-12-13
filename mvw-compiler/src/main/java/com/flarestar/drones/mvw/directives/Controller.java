package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.view.scope.Property;

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

    private final static Pattern controllerAttributeRegex = Pattern.compile("(#)?([\\w.$]+)\\s+as\\s+(\\w+)");

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

        if (isInjected) {
            String injectedPropertyName = "_" + node.id + "_" + controllerScopeProperty;
            context.addInjectedProperty(controllerClass, injectedPropertyName);

            initialValue = context.getLayoutBuilderSimpleClassName() + ".this." + injectedPropertyName;
        } else {
            initialValue = "new " + controllerClass + "(owner.getContext())";
        }

        properties.add(new Property(controllerScopeProperty, controllerClass, Property.BindType.NONE, initialValue, this));
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
