package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.annotations.directive.DynamicDirective;
import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.compilerutilities.exceptions.BaseExpressionException;
import com.flarestar.drones.layout.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DirectiveName("ng-repeat")
@DirectiveMatcher(AttributeMatcher.class)
@DynamicDirective
public class Repeat extends Directive {
    private static final Pattern REPEAT_ATTRIBUTE_REGEX = Pattern.compile("(\\w+)\\s+in\\s+(.+)");

    private final String iterationScopeVariable;
    private final String iterableExpression;
    private final TypeMirror iterableType;
    private final TypeMirror iterationScopeVariableType;

    public Repeat(ViewNode node) throws LayoutFileException {
        super(node);

        Matcher m = REPEAT_ATTRIBUTE_REGEX.matcher(node.attributes.get("ng-repeat"));
        if (!m.matches()) {
            throw new InvalidLayoutAttributeValue(
                "ng-repeat expects input like 'var in expr' where expr evaluates to an iterable.");
        }

        iterationScopeVariable = m.group(1);
        iterableExpression = m.group(2);

        TypeInferer inferer = TypeInferer.getInstance();

        try {
            iterableType = node.getScopeDefinition().getTypeOfExpression(iterableExpression);
            iterationScopeVariableType = inferer.getValueTypeOf(iterableType);
        } catch (BaseExpressionException ex) {
            throw new LayoutFileException("Invalid ng-repeat expression", ex);
        }
    }

    @Override
    public String beforeScopeCreated() throws LayoutFileException {
        return "for (final " + iterationScopeVariableType.toString() + " " + iterationScopeVariable + " : " + iterableExpression + ") {\n";
    }

    @Override
    public String afterViewAdded() throws LayoutFileException {
        return "}\n";
    }
}
