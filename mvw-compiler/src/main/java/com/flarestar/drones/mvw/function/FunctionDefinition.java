package com.flarestar.drones.mvw.function;

import com.flarestar.drones.mvw.annotations.Function;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TODO
 */
public class FunctionDefinition {

    public static class Parameter {
        private String name;
        private String type;

        public Parameter(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    private TypeElement element;
    private String name;
    private ExecutableElement invokeMethod;
    private List<Parameter> invokeParameters;
    private boolean isInjected;

    public FunctionDefinition(TypeElement element) throws InvalidUserFunctionClass {
        this.element = element;

        Function annotation = element.getAnnotation(Function.class);
        this.name = annotation.value();

        this.invokeMethod = findInvokeMethod();
        this.invokeParameters = getInvokeParameters();
        this.isInjected = checkIfHasInjectableConstructor();
    }

    private boolean checkIfHasInjectableConstructor() {
        for (Element member : element.getEnclosedElements()) {
            if (!(member instanceof ExecutableElement)) {
                continue;
            }

            if (!member.getSimpleName().toString().equals("<init>")) {
                continue;
            }

            if (member.getAnnotation(Inject.class) != null) {
                return true;
            }
        }
        return false;
    }

    private List<Parameter> getInvokeParameters() {
        List<Parameter> parameters = new ArrayList<>();
        for (VariableElement element : invokeMethod.getParameters()) {
            parameters.add(new Parameter(element.getSimpleName().toString(), element.asType().toString()));
        }
        return parameters;
    }

    public String getFunctionClassName() {
        return element.getQualifiedName().toString();
    }

    public String getResultType() {
        return invokeMethod.getReturnType().toString();
    }

    public List<Parameter> getParameters() {
        return invokeParameters;
    }

    public TypeElement getElement() {
        return element;
    }

    public String getName() {
        return name;
    }

    public ExecutableElement getInvokeMethod() {
        return invokeMethod;
    }

    private ExecutableElement findInvokeMethod() throws InvalidUserFunctionClass {
        ExecutableElement result = null;
        for (Element member : element.getEnclosedElements()) {
            if (!(member instanceof ExecutableElement)) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement) member;
            if (executableElement.getSimpleName().toString().equals("invoke")) {
                if (this.invokeMethod != null) {
                    throw new InvalidUserFunctionClass("Multiple invoke methods in '" + name + "' function (" +
                        element.getQualifiedName().toString() + "). There should only be one.");
                }

                result = executableElement;
            }
        }

        if (result == null) {
            throw new InvalidUserFunctionClass("User function '" + name + "' (" + element.getQualifiedName().toString()
                + ") is missing an invoke method.");
        }

        return result;
    }

    public boolean isInjected() {
        return isInjected;
    }
}