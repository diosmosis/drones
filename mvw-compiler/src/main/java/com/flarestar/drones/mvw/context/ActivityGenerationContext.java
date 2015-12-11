package com.flarestar.drones.mvw.context;

import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.compilerutilities.ProjectSniffer;

import javax.lang.model.element.TypeElement;

/**
 * TODO
 */
public class ActivityGenerationContext extends GenerationContext {
    private final String activityClassName;
    private final String layoutBuilderClassName;
    private final String layoutBuilderSimpleClassName;
    private final String activityPackage;

    public ActivityGenerationContext(TypeElement activityClassElement, ProjectSniffer projectSniffer)
        throws InvalidManifestException {
        super(projectSniffer);

        activityClassName = activityClassElement.getQualifiedName().toString();
        layoutBuilderClassName = activityClassName + "LayoutBuilderDrone";
        activityPackage = activityClassName.substring(0, activityClassName.lastIndexOf('.'));
        layoutBuilderSimpleClassName = getSimpleClassName(layoutBuilderClassName);
    }

    public String getActivityClassName() {
        return activityClassName;
    }

    public String getActivityPackage() {
        return activityPackage;
    }

    public String getLayoutBuilderClassName() {
        return layoutBuilderClassName;
    }

    @Override
    public String getLayoutBuilderSimpleClassName() {
        return layoutBuilderSimpleClassName;
    }

    private String getSimpleClassName(String screenClassName) {
        int lastDot = screenClassName.lastIndexOf('.');
        return screenClassName.substring((lastDot == -1 ? 0 : lastDot) + 1);
    }
}
