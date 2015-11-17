package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveMatcher(AttributeMatcher.class)
public class Controller extends Directive {
    public static class InvalidControllerAttribute extends LayoutFileException {
        public InvalidControllerAttribute(String message) {
            super(message);
        }
    }

    private final static Pattern controllerAttributeRegex = Pattern.compile("(\\w+)\\s+as\\s+(\\w+)");

    @Override
    public String getDirectiveName() {
        return "ng-controller";
    }

    @Override
    public List<ScopeDefinition.Property> getScopeProperties(ViewNode node) throws LayoutFileException {
        List<ScopeDefinition.Property> result = super.getScopeProperties(node);

        // TODO: inefficient, we have to parse the regex twice (here & in onViewCreated)
        Matcher match = parseAttribute(node);

        String controllerClass = match.group(1);
        String controllerScopeProperty = match.group(2);

        result.add(new ScopeDefinition.Property(controllerScopeProperty, controllerClass, this));
        return result;
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

    // TODO: would be better in the future to use Dagger DI to create controllers instead of manually creating
    // new instances.
    @Override
    public void onViewCreated(ViewNode node, StringBuilder result) throws LayoutFileException {
        Matcher match = parseAttribute(node);

        String controllerClass = match.group(1);
        String controllerScopeProperty = match.group(2);

        result.append(node.getScopeVarName());
        result.append(".");
        result.append(controllerScopeProperty);
        result.append(" = new ");
        result.append(controllerClass);
        result.append("(realScreen);\n");
    }
}
