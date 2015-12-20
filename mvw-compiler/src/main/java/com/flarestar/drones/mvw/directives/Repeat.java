package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DynamicDirective;
import com.flarestar.drones.mvw.annotations.directive.ScopeProperties;
import com.flarestar.drones.mvw.compilerutilities.exceptions.BaseExpressionException;
import com.flarestar.drones.mvw.model.ViewFactory;
import com.flarestar.drones.mvw.processing.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.RangeViewFactory;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.views.scope.watcher.CollectionWatcher;
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

    public class RepeatViewFactory extends ViewFactory {
        private ViewNode view;

        public RepeatViewFactory(ViewNode view) {
            super(RepeatViewFactoryRenderable.class);
            this.view = view;
        }

        public Repeat getDirective() {
            return Repeat.this;
        }

        public ViewNode getView() {
            return view;
        }
    }

    public static class RepeatViewFactoryRenderable extends RangeViewFactory {
        private String iterationScopeVariable;
        private String iterableExpression;
        private TypeMirror iterableType;
        private TypeMirror iterationScopeVariableType;
        private String scopeClassName;

        public RepeatViewFactoryRenderable(MakeViewBody makeViewBody, RepeatViewFactory viewFactoryModel) {
            super(makeViewBody, viewFactoryModel);

            Repeat repeatDirective = viewFactoryModel.getDirective();

            iterationScopeVariable = repeatDirective.iterationScopeVariable;
            iterableExpression = repeatDirective.iterableExpression;
            iterableType = repeatDirective.iterableType;
            iterationScopeVariableType = repeatDirective.iterationScopeVariableType;
            scopeClassName = viewFactoryModel.getView().scopeDefinition.getScopeClassName();
        }

        @AssistedInject
        public RepeatViewFactoryRenderable(@Assisted Object[] args) {
            this((MakeViewBody)args[0], (RepeatViewFactory)args[1]);
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
        super.manipulateViewNode(node);

        Matcher m = REPEAT_ATTRIBUTE_REGEX.matcher(node.element.attr("ng-repeat"));
        if (!m.matches()) {
            throw new InvalidLayoutAttributeValue(
                "ng-repeat expects input like 'var in expr' where expr evaluates to an iterable.");
        }

        iterationScopeVariable = m.group(1);
        iterableExpression = m.group(2);

        node.scopeDefinition.watchers.add(new WatcherDefinition(
            CollectionWatcher.class,
            "return " + iterableExpression + ";",
            "if (oldValue == newValue) return;\n" +
                "com.flarestar.drones.mvw.directive.RepeatUtilities.queueOnValuesChanged(_parentView, (RangeViewFactory<?>)_viewFactory);\n",
            true
        ));
    }

    @Override
    public void beforeGeneration(ViewNode node) throws LayoutFileException {
        // TODO: shouldn't do this here, should do it in manipulateViewNode
        try {
            iterableType = typeInferer.getTypeOfExpression(node.scopeDefinition, iterableExpression);
            iterationScopeVariableType = typeInferer.getValueTypeOf(iterableType);
        } catch (BaseExpressionException ex) {
            throw new LayoutFileException("Invalid ng-repeat expression", ex);
        }

        node.scopeDefinition.addProperty(new Property(iterationScopeVariable, iterationScopeVariableType.toString(),
            Property.BindType.LOCAL_VAR, "_item", false, this));
    }

    @Override
    public ViewFactory getViewFactoryToUse(ViewNode viewNode) {
        return new RepeatViewFactory(viewNode);
    }
}
