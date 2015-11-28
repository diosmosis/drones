package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.annotations.directive.*;
import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.layout.view.scope.Event;
import com.flarestar.drones.layout.view.scope.Property;

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

    public String afterViewAdded(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String afterChildrenAdded(ViewNode node) throws LayoutFileException {
        return "";
    }

    public String beforeReturnResult(ViewNode node) throws LayoutFileException {
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
}
