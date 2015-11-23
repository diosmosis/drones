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
import com.google.inject.Inject;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: ng-repeat should add the item + other vars (like $index/$first/$last) to the scope so they are available to angular expressions
@DirectiveName("ng-repeat")
@DirectiveMatcher(AttributeMatcher.class)
@DynamicDirective
public class Repeat extends Directive {
    private static final Pattern REPEAT_ATTRIBUTE_REGEX = Pattern.compile("(\\w+)\\s+in\\s+(.+)");

    private final String iterationScopeVariable;
    private final String iterableExpression;
    private TypeMirror iterableType;
    private TypeMirror iterationScopeVariableType;

    @Inject
    private TypeInferer typeInferer;

    public Repeat(ViewNode node) throws LayoutFileException {
        super(node);

        Matcher m = REPEAT_ATTRIBUTE_REGEX.matcher(node.attributes.get("ng-repeat"));
        if (!m.matches()) {
            throw new InvalidLayoutAttributeValue(
                "ng-repeat expects input like 'var in expr' where expr evaluates to an iterable.");
        }

        iterationScopeVariable = m.group(1);
        iterableExpression = m.group(2);
    }

    public void postConstruct() throws LayoutFileException {
        try {
            iterableType = typeInferer.getTypeOfExpression(node.getScopeDefinition(), iterableExpression);
            iterationScopeVariableType = typeInferer.getValueTypeOf(iterableType);
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

    @Override
    public String beforeReturnResult() throws LayoutFileException {
        StringBuilder result = new StringBuilder();
        result.append("scope.watch(new com.flarestar.drones.views.scope.CollectionWatcher() {\n");
        result.append("    @Override\n");
        result.append("    public Object getWatchValue(Scope<?> _scope) {\n");

        {
            result.append("        return ");
            result.append(iterableExpression);
            result.append(";\n");
        }

        result.append("    }\n");
        result.append("\n");
        result.append("    @Override\n");
        result.append("    public void onValueChanged(Object newValue, Object oldValue, Scope<?> _scope) {\n");
        // TODO: we should only do this ONCE per tree. and need to take into context child views. do createChildren calls for them
        // get invalidated when a parent has to be changed? not sure....
        result.append("        _parentView.createChildren();\n");
        result.append("    }\n");
        result.append("});\n");
        return result.toString();
    }
}
