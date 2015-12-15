package com.flarestar.drones.base.renderables;

import com.flarestar.drones.base.generation.ClassRenderable;

import javax.lang.model.element.TypeElement;

/**
 * TODO
 */
public class ActivityModule extends GeneratedClassModel implements ClassRenderable {
    public ActivityModule(TypeElement element) {
        super(element);
    }

    @Override
    public String getTemplate() {
        return "templates/ActivityModule.twig";
    }

    @Override
    public String getModelAttribute() {
        return "module";
    }

    @Override
    protected String computeGeneratedClassName() {
        return getElement().getQualifiedName().toString() + "ActivityModule";
    }

    public String getGeneratedClassNameLc() {
        String activityModuleClass = getGeneratedClassName();
        String activityModuleClassSimpleName = activityModuleClass.substring(activityModuleClass.lastIndexOf('.') + 1);
        return lcFirst(activityModuleClassSimpleName);
    }

    private String lcFirst(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
