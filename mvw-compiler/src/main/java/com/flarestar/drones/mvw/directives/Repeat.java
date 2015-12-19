package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DynamicDirective;
import com.flarestar.drones.mvw.annotations.directive.ScopeProperties;
import com.flarestar.drones.mvw.compilerutilities.exceptions.BaseExpressionException;
import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.RangeViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.views.scope.CollectionWatcher;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.lang.model.type.TypeMirror;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: ng-repeat should add the item + other vars (like $index/$first/$last) to the scope so they are available to angular expressions
@DirectiveName("ng-repeat")
@DirectiveMatcher(AttributeMatcher.class)
@DynamicDirective
@ScopeProperties({"int $index = /_index"})
public class Repeat extends Directive {

    // TODO: use separate template + extends, make RangeViewFactory abstract.
    public static class RepeatViewFactory extends RangeViewFactory {
        private String iterationScopeVariable;
        private String iterableExpression;
        private TypeMirror iterableType;
        private TypeMirror iterationScopeVariableType;
        private String scopeClassName;

        @AssistedInject
        public RepeatViewFactory(@Assisted MakeViewBody makeViewBody) {
            super(makeViewBody);

            Repeat repeatDirective = null;
            for (Directive directive : makeViewBody.getView().directives) {
                if (directive instanceof Repeat) {
                    repeatDirective = (Repeat)directive;
                    break;
                }
            }

            if (repeatDirective == null) {
                throw new IllegalStateException("RepeatViewFactory cannot find the view's Repeat directive. This should not happen.");
            }

            iterationScopeVariable = repeatDirective.iterationScopeVariable;
            iterableExpression = repeatDirective.iterableExpression;
            iterableType = repeatDirective.iterableType;
            iterationScopeVariableType = repeatDirective.iterationScopeVariableType;
            scopeClassName = makeViewBody.getView().scopeDefinition.getScopeClassName();
        }

        public String getScopeClassName() {
            return scopeClassName;
        }

        public String getIterationScopeVariable() {
            return iterationScopeVariable;
        }

        public String getIterableExpression() {
            return iterableExpression;
        }

        public TypeMirror getIterableType() {
            return iterableType;
        }

        public TypeMirror getIterationScopeVariableType() {
            return iterationScopeVariableType;
        }

        @Override
        public String getTemplate() {
            return "templates/repeatViewFactory.twig";
        }
    }

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

        node.scopeDefinition.addProperty(new Property(iterationScopeVariable, iterationScopeVariableType.toString(),
            Property.BindType.LOCAL_VAR, "_item", this));
    }

    @Override
    public Class<? extends ViewFactory> getViewFactoryToUse() {
        return RepeatViewFactory.class;
    }
}
