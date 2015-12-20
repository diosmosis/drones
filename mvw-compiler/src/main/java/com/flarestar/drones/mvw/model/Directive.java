package com.flarestar.drones.mvw.model;

import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.*;
import com.flarestar.drones.mvw.directives.Controller;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.flarestar.drones.mvw.processing.parser.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 */
public abstract class Directive {

    protected final GenerationContext context;

    private final List<Property> properties = new ArrayList<>();

    @Inject
    protected TypeInferer typeInferer;

    public Directive(GenerationContext context) throws LayoutFileException {
        this.context = context;

        ScopeProperties annotation = getClass().getAnnotation(ScopeProperties.class);
        if (annotation != null) {
            for (String propertyDescriptor : annotation.value()) {
                Property property = Property.makeFromDescriptor(propertyDescriptor, this);
                properties.add(property);
            }
        }
    }

    public final List<Property> getScopeProperties() {
        return properties;
    }

    public void postConstruct() throws LayoutFileException {
        // empty
    }

    public String getViewClassName() {
        DirectiveView annotation = getClass().getAnnotation(DirectiveView.class);
        return annotation == null ? null : annotation.view().getName();
    }

    public String getHookCode(String hookName, ViewNode node)
            throws LayoutFileException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method hookMethod = getClass().getMethod(hookName, ViewNode.class);
        return (String)hookMethod.invoke(this, node);
    }

    public void beforeGeneration(ViewNode node) throws LayoutFileException {
        // empty
    }

    public boolean isDynamic() {
        DynamicDirective annotation = getClass().getAnnotation(DynamicDirective.class);
        return annotation != null;
    }

    public void manipulateViewNode(ViewNode viewNode) throws LayoutFileException {
        addPropertiesFromAnnotation(viewNode);
        addPropertyIfDirectiveControllerUsed(viewNode);
    }

    private void addPropertiesFromAnnotation(ViewNode viewNode) throws LayoutFileException {
        // only add isolate directive properties to the directive's root nodes. child nodes that use
        // isolate directives have their own scopes.
        if (!viewNode.isDirectiveRoot && isIsolateDirective()) {
            return;
        }

        for (Property property : properties) {
            viewNode.scopeDefinition.addProperty(property);
        }
    }

    private void addPropertyIfDirectiveControllerUsed(ViewNode viewNode) throws LayoutFileException {
        if (!isIsolateDirective()) {
            return;
        }

        DirectiveController annotation = getClass().getAnnotation(DirectiveController.class);
        if (annotation == null) {
            return;
        }

        Controller.AttributeProcessor processor = new Controller.AttributeProcessor(context, this);

        Controller.AttributeProcessor.ParseResult result = processor.parse(annotation.value());
        if (viewNode.isDirectiveRoot) {
            result.setIsInjected(false);
        }

        if (result.isInjected()) {
            TypeElement controllerClass = typeInferer.getTypeElementFor(result.getControllerClass());
            if (controllerClass.getAnnotation(Singleton.class) != null) {
                throw new Controller.InvalidControllerAttribute(getDirectiveName() + ": injected directive controller '"
                    + result.getControllerClass() + "' should not be marked as @Singleton since a new controller must "
                    + "be created each time the directie is used.");
            }
        }

        processor.process(result, viewNode);
    }

    public String getDirectiveName() {
        return getDirectiveName(getClass());
    }

    public static String getDirectiveName(Class<?> directiveClass) {
        DirectiveName annotation = directiveClass.getAnnotation(DirectiveName.class);
        if (annotation == null) {
            throw new InvalidDirectiveClassException("@DirectiveName is missing on '" + directiveClass.getName()
                + "' directive.");
        }
        return annotation.value();
    }

    public boolean isIsolateDirective() {
        return getClass().getAnnotation(IsolateDirective.class) != null;
    }

    public boolean hasTransclude() {
        return Directive.hasTransclude(getClass());
    }

    public static boolean hasTransclude(Class<?> klass) {
        IsolateDirective annotation = klass.getAnnotation(IsolateDirective.class);
        return annotation != null && annotation.transclude();
    }

    public void removePropertiesIf(Predicate<Property> predicate) {
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property prop = iterator.next();
            if (predicate.apply(prop)) {
                iterator.remove();
            }
        }
    }

    public ViewFactory getViewFactoryToUse(ViewNode viewNode) {
        return null;
    }
}
