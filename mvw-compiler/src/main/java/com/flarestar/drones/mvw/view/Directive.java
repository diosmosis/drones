package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.*;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.mvw.view.scope.Event;
import com.flarestar.drones.mvw.view.scope.Property;
import com.flarestar.drones.mvw.view.scope.WatcherDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class Directive {

    protected final GenerationContext context;
    protected final List<Property> properties = new ArrayList<>();
    protected final List<Event> events = new ArrayList<>();
    protected final List<WatcherDefinition> watchers = new ArrayList<>();

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

    public void postConstruct() throws LayoutFileException {
        // empty
    }

    public List<Property> getScopeProperties() {
        return properties;
    }

    public List<WatcherDefinition> getWatchers() {
        return watchers;
    }

    public List<Event> getEvents() {
        return events;
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

    public String afterViewCreated(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String beforeScopeCreated(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String beforeViewCreated(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String afterChildrenAdded(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String beforeReturnResult(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String beginViewFactory(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String endViewFactory(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String onCreatedNewScope(ViewNode node) throws LayoutFileException {
        return "";
    }

    public void manipulateViewNode(ViewNode viewNode) throws LayoutFileException {
        // empty
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
}
