package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DynamicDirective;
import com.flarestar.drones.mvw.annotations.directive.ScopeProperties;
import com.flarestar.drones.mvw.compilerutilities.exceptions.BaseExpressionException;
import com.flarestar.drones.mvw.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.renderables.viewfactory.RangeViewFactory;
import com.flarestar.drones.mvw.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.view.scope.Property;
import com.flarestar.drones.mvw.renderables.scope.WatcherDefinition;
import com.flarestar.drones.views.scope.CollectionWatcher;

import javax.lang.model.type.TypeMirror;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: ng-repeat should add the item + other vars (like $index/$first/$last) to the scope so they are available to angular expressions
@DirectiveName("ng-repeat")
@DirectiveMatcher(AttributeMatcher.class)
@DynamicDirective
@ScopeProperties({"int $index = -1"})
public class Repeat extends Directive {
    private static final Pattern REPEAT_ATTRIBUTE_REGEX = Pattern.compile("(\\w+)\\s+in\\s+(.+)");

    private String iterationScopeVariable;
    private String iterableExpression;
    private TypeMirror iterableType;
    private TypeMirror iterationScopeVariableType;

    public Repeat(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void manipulateViewNode(ViewNode node) throws LayoutFileException {
        Matcher m = REPEAT_ATTRIBUTE_REGEX.matcher(node.element.attr("ng-repeat"));
        if (!m.matches()) {
            throw new InvalidLayoutAttributeValue(
                "ng-repeat expects input like 'var in expr' where expr evaluates to an iterable.");
        }

        iterationScopeVariable = m.group(1);
        iterableExpression = m.group(2);
    }

    @Override
    public void beforeGeneration(ViewNode node) throws LayoutFileException {
        try {
            iterableType = typeInferer.getTypeOfExpression(node.scopeDefinition, iterableExpression);
            iterationScopeVariableType = typeInferer.getValueTypeOf(iterableType);
        } catch (BaseExpressionException ex) {
            throw new LayoutFileException("Invalid ng-repeat expression", ex);
        }

        watchers.add(new WatcherDefinition(
            CollectionWatcher.class,
            "return " + iterableExpression + ";",
            "if (oldValue == newValue) return;\n" +
            "com.flarestar.drones.mvw.directive.RepeatUtilities.queueOnValuesChanged(_parentView, (RangeViewFactory<"
                + iterationScopeVariableType.toString() + ">)_viewFactory);\n",
            true
        ));

        node.scopeDefinition.addProperty(new Property(iterationScopeVariable, iterationScopeVariableType.toString(), null, null, this));
    }

    @Override
    public ViewFactory getViewFactoryToUse(ViewNode view, Directive directiveRoot) {
        String scopeClassName = view.scopeDefinition.getScopeClassName();
        return new RangeViewFactory(
            view.createMakeViewBodyRenderable(directiveRoot, "scope." + iterationScopeVariable + " = _item;\nscope.$index = _index;"),
            iterationScopeVariableType,
            "return " + iterableExpression + ";",
            "return ((" + scopeClassName + ")scope)." + iterationScopeVariable + ";",
            scopeClassName + " _realScope = (" + scopeClassName + ")scope;\n_realScope.$index = index;"
        );
    }
}
