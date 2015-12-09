package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DynamicDirective;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.compilerutilities.exceptions.BaseExpressionException;
import com.flarestar.drones.mvw.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.matchers.AttributeMatcher;
import com.google.inject.Inject;

import javax.lang.model.type.TypeMirror;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: ng-repeat should add the item + other vars (like $index/$first/$last) to the scope so they are available to angular expressions
@DirectiveName("ng-repeat")
@DirectiveMatcher(AttributeMatcher.class)
@DynamicDirective
public class Repeat extends Directive {
    private static final Pattern REPEAT_ATTRIBUTE_REGEX = Pattern.compile("(\\w+)\\s+in\\s+(.+)");

    private String iterationScopeVariable;
    private String iterableExpression;
    private TypeMirror iterableType;
    private TypeMirror iterationScopeVariableType;

    @Inject
    private TypeInferer typeInferer;

    public Repeat(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void beforeGeneration(ViewNode node) throws LayoutFileException {
        Matcher m = REPEAT_ATTRIBUTE_REGEX.matcher(node.attributes.get("ng-repeat"));
        if (!m.matches()) {
            throw new InvalidLayoutAttributeValue(
                "ng-repeat expects input like 'var in expr' where expr evaluates to an iterable.");
        }

        iterationScopeVariable = m.group(1);
        iterableExpression = m.group(2);

        try {
            iterableType = typeInferer.getTypeOfExpression(node.scopeDefinition, iterableExpression);
            iterationScopeVariableType = typeInferer.getValueTypeOf(iterableType);
        } catch (BaseExpressionException ex) {
            throw new LayoutFileException("Invalid ng-repeat expression", ex);
        }
    }

    @Override
    public String beginViewFactory(ViewNode node) throws LayoutFileException {
        StringBuilder result = new StringBuilder();

        {
            result.append("return new RangeViewFactory<");
            result.append(iterationScopeVariableType.toString());
            result.append(">() {\n");
        }

        result.append("@Override\n");
        result.append("protected Iterable getCollection() {\n");

        {
            result.append("    return ");
            result.append(iterableExpression);
            result.append(";\n");
        }

        result.append("}\n");

        result.append("@Override\n");

        {
            result.append("protected View makeView(");
            result.append(iterationScopeVariableType.toString());
            result.append(" currentValue, final int index) {\n");
        }

        {
            result.append("final ");
            result.append(iterationScopeVariableType.toString());
            result.append(' ');
            result.append(iterationScopeVariable);
            result.append(" = currentValue;\n");
        }

        return result.toString();
    }

    @Override
    public String endViewFactory(ViewNode node) throws LayoutFileException {
        return "    }\n};\n";
    }

    @Override
    public String beforeReturnResult(ViewNode node) throws LayoutFileException {
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
        result.append("        if (newValue == oldValue) return;\n");
        result.append("        _parentView.removeAllViews();\n");
        result.append("    }\n");
        result.append("});\n");
        return result.toString();
    }
}
