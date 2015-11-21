package com.flarestar.drones.layout.compilerutilities;

import com.flarestar.drones.layout.compilerutilities.exceptions.*;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

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

/**
 * TODO
 */
public class TypeInferer {
    private static TypeInferer instance;

    private TypeMirror iterableBaseTypeMirror;
    private ProcessingEnvironment processingEnvironment;
    private String basePackage = "";

    public TypeInferer(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;

        TypeElement iterableBaseElement = processingEnvironment.getElementUtils().getTypeElement(Iterable.class.getName());
        if (iterableBaseElement == null) {
            throw new RuntimeException("Unexpected error: cannot find Iterable class as TypeElement.");
        }

        this.iterableBaseTypeMirror = iterableBaseElement.asType();
    }

    public static TypeInferer getInstance() { // TODO: shouldn't be singleton.
        if (TypeInferer.instance == null) {
            throw new IllegalStateException("TypeInferer singleton has not been created.");
        }
        return instance;
    }

    public static void createInstance(ProcessingEnvironment processingEnv) {
        if (TypeInferer.instance != null) {
            throw new IllegalStateException("TypeInferer singleton has already been created.");
        }

        TypeInferer.instance = new TypeInferer(processingEnv);
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
}
