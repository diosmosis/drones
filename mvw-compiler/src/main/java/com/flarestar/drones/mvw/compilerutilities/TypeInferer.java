package com.flarestar.drones.mvw.compilerutilities;

import com.flarestar.drones.mvw.compilerutilities.exceptions.*;
import com.flarestar.drones.mvw.view.scope.Property;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 */
@Singleton
public class TypeInferer {
    private static final Pattern EXPRESSION_START_REGEX = Pattern.compile("([a-zA-Z0-9_$]+)(.*)");

    private ProcessingEnvironment processingEnvironment;
    private String basePackage = "";

    @Inject
    public TypeInferer(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public TypeMirror getTypeMirrorFor(String type) {
        if (type.indexOf('.') == -1) {
            type = basePackage + '.' + type;
        }

        TypeElement result = processingEnvironment.getElementUtils().getTypeElement(type);
        if (result == null) {
            throw new CannotFindTypeMirror(type);
        }
        return result.asType();
    }

    public TypeMirror getTypeOfExpression(TypeMirror type, String expression, ScopeDefinition currentScope)
            throws InvalidExpression, InvalidTypeException, InvalidTypeExpression, CannotFindProperty, CannotFindMethod {
        ExpressionParser parser = new ExpressionParser(type, expression, currentScope, this);
        return parser.getResultType();
    }

    public TypeMirror getValueTypeOf(String iterableType) throws InvalidTypeException {
        return getValueTypeOf(getTypeMirrorFor(iterableType));
    }

    public TypeMirror getValueTypeOf(TypeMirror iterableType) throws InvalidTypeException {
        if (iterableType.getKind() == TypeKind.ARRAY) {
            return ((ArrayType)iterableType).getComponentType();
        } else if (iterableType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType)iterableType;

            for (TypeMirror baseType : getAllSuperTypes(declaredType)) {
                if (baseType.toString().startsWith(Iterable.class.getName())) {
                    return ((DeclaredType)baseType).getTypeArguments().get(0);
                }
            }

            throw new InvalidTypeException("type '" + iterableType + "' does not implement Iterable.");
        } else {
            throw new InvalidTypeException("type '" + iterableType + "' is not iterable (kind is '"
                + iterableType.getKind() + "').");
        }
    }

    public Set<TypeMirror> getAllSuperTypes(DeclaredType type) {
        HashSet<TypeMirror> types = new HashSet<>();
        getAllSuperTypesWithSet(type, types);
        return types;
    }

    private void getAllSuperTypesWithSet(DeclaredType type, HashSet<TypeMirror> types) {
        for (TypeMirror supertype : processingEnvironment.getTypeUtils().directSupertypes(type)) {
            types.add(supertype);
            getAllSuperTypesWithSet((DeclaredType)supertype, types);
        }
    }

    public TypeMirror getPropertyTypeOf(TypeMirror type, String name) throws CannotFindProperty {
        for (Element element : ((DeclaredType)type).asElement().getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }

            VariableElement fieldElement = (VariableElement)element;
            if (!fieldElement.getSimpleName().toString().equals(name)) {
                continue;
            }

            return fieldElement.asType();
        }

        throw new CannotFindProperty("Cannot find property '" + name + "' in '" + type.toString() + "'.");
    }

    public TypeMirror getMethodReturnTypeOf(TypeMirror type, String name, List<TypeMirror> parameters)
            throws CannotFindMethod {
        for (Element element : ((DeclaredType)type).asElement().getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement)element;
            if (!executableElement.getSimpleName().toString().equals(name)) {
                continue;
            }

            if (!parametersMatch(executableElement, parameters)) {
                continue;
            }

            return executableElement.getReturnType();
        }

        throw new CannotFindMethod(name, type, parameters);
    }

    private boolean parametersMatch(ExecutableElement element, List<TypeMirror> requestedParameters) {
        List<? extends VariableElement> parameters = element.getParameters();

        if (parameters.size() != requestedParameters.size()) {
            return false;
        }

        Iterator<? extends VariableElement> i1 = parameters.iterator();
        Iterator<TypeMirror> i2 = requestedParameters.iterator();

        while (i1.hasNext()) {
            if (!processingEnvironment.getTypeUtils().isSameType(i1.next().asType(), i2.next())) {
                return false;
            }
        }

        return true;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public TypeMirror getTypeOfExpression(ScopeDefinition context, String expression)
        throws InvalidTypeExpression, InvalidExpression, InvalidTypeException, CannotFindProperty, CannotFindMethod {

        if (expression.startsWith("scope.")) {
            expression = expression.substring(6);
        }

        TypeMirror type = null;

        ScopeDefinition scope = context;
        while (scope != null) {
            Matcher m = EXPRESSION_START_REGEX.matcher(expression);
            if (!m.matches()) {
                throw new RuntimeException("Unexpected error, cannot parse expression start: " + expression);
            }

            String propertyName = m.group(1);
            Property property = scope.allProperties().get(propertyName);
            if (property == null) {
                throw new InvalidTypeExpression(expression, "no property named '" + propertyName + "'");
            }
            expression = m.group(2);

            // TODO: if someone stores a scope as a property in a scope, this won't work out.
            if (property.type.equals("_parent")) {
                scope = scope.getParentScope();
            } else {
                type = getTypeMirrorFor(property.type);
                scope = null;
            }
        }

        if (type == null) {
            // TODO: We can't get a TypeMirror from a generic string, eg List<String> and we can't get
            //       TypeMirrors for Scope types, so for now disabling using scope types as results in expressions.
            throw new InvalidTypeExpression(expression, "scope types not allowed in this context");
        }

        return getTypeOfExpression(type, expression, context);
    }

    public boolean isAssignable(String type, String baseType) {
        return processingEnvironment.getTypeUtils().isAssignable(getTypeMirrorFor(type), getTypeMirrorFor(baseType));
    }
}
