package com.flarestar.drones.base.renderables;

import org.jtwig.JtwigModelMap;

import javax.lang.model.element.TypeElement;

/**
 * TODO
 */
public abstract class GeneratedClassModel {
    private TypeElement element;

    private String fullModuleClassName;
    private String generatedPackage;
    private String generatedClassName;

    public GeneratedClassModel(TypeElement element) {
        this.element = element;

        fullModuleClassName = computeGeneratedClassName();

        int lastDot = fullModuleClassName.lastIndexOf('.');
        generatedPackage = fullModuleClassName.substring(0, lastDot == -1 ? fullModuleClassName.length() : lastDot);
        generatedClassName = fullModuleClassName.substring((lastDot == -1 ? 0 : lastDot) + 1);
    }

    protected abstract String computeGeneratedClassName();

    public TypeElement getElement() {
        return element;
    }

    public String getGeneratedPackage() {
        return generatedPackage;
    }

    public String getGeneratedClassName() {
        return generatedClassName;
    }

    public String getFullGeneratedClassName() {
        return fullModuleClassName;
    }
}
