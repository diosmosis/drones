package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.annotations.directive.*;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.directive.exceptions.InvalidDirectiveClassException;
import com.flarestar.drones.layout.view.scope.Property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class Directive {

    protected final ViewNode node;

    public Directive(ViewNode node) {
        this.node = node;
    }

    public void postConstruct() throws LayoutFileException {
        // empty
    }

    public String getViewClassName() {
        DirectiveView annotation = getClass().getAnnotation(DirectiveView.class);
        return annotation == null ? null : annotation.view().getName();
    }

    public String getHookCode(String hookName)
            throws LayoutFileException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method hookMethod = getClass().getMethod(hookName);
        return (String)hookMethod.invoke(this);
    }

    public List<Property> getScopeProperties() throws LayoutFileException {
        List<Property> result = new ArrayList<>();

        ScopeProperties annotation = getClass().getAnnotation(ScopeProperties.class);
        if (annotation == null) {
            return result;
        }

        for (String propertyDescriptor : annotation.value()) {
            Property property = Property.makeFromDescriptor(propertyDescriptor, this);
            result.add(property);
        }

        return result;
    }

    public boolean isDynamic() {
        DynamicDirective annotation = getClass().getAnnotation(DynamicDirective.class);
        return annotation != null;
    }

    public String afterViewCreated() throws LayoutFileException {
        return "";
    }

    public String beforeScopeCreated() throws LayoutFileException {
        return "";
    }

    public String beforeViewCreated() throws LayoutFileException {
        return "";
    }

    public String afterViewAdded() throws LayoutFileException {
        return "";
    }

    public String afterChildrenAdded() throws LayoutFileException {
        return "";
    }

    public String beforeReturnResult() throws LayoutFileException {
        return "";
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
