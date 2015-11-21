package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveName("ng-controller")
@DirectiveMatcher(AttributeMatcher.class)
public class Controller extends Directive {
    public Controller(ViewNode node) {
        super(node);
    }

    public static class InvalidControllerAttribute extends LayoutFileException {
        public InvalidControllerAttribute(String message) {
            super(message);
        }
    }

    private final static Pattern controllerAttributeRegex = Pattern.compile("(\\w+)\\s+as\\s+(\\w+)");

    @Override
    public List<ScopeDefinition.Property> getScopeProperties() throws LayoutFileException {
        List<ScopeDefinition.Property> result = super.getScopeProperties();

        Matcher match = parseAttribute();

        String controllerClass = match.group(1);
        String controllerScopeProperty = match.group(2);

        result.add(new ScopeDefinition.Property(controllerScopeProperty, controllerClass,
            "new " + controllerClass + "(_screen)", this));
        return result;
    }

    private Matcher parseAttribute() throws InvalidControllerAttribute {
        String attributeValue = node.attributes.get("ng-controller");
        Matcher match = controllerAttributeRegex.matcher(attributeValue);
        if (!match.matches()) {
            throw new InvalidControllerAttribute("Invalid ng-controller attribute: " + attributeValue +
                ". Expected something like 'Controller as scopeProperty'.");
        }

        return match;
    }

    // TODO: would be better in the future to use Dagger DI to create controllers instead of manually creating
    // new instances.
}
