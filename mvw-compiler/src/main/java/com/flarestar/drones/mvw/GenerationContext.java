package com.flarestar.drones.mvw;

import com.flarestar.drones.mvw.android.Manifest;
import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.compilerutilities.ProjectSniffer;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

public class GenerationContext {

    public static class InjectedProperty {
        public final String type;
        public final String name;

        public InjectedProperty(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private final String activityClassName;
    private final String layoutBuilderClassName;
    private final String layoutBuilderSimpleClassName;
    private final String activityPackage;
    private final String applicationPackage;

    private List<InjectedProperty> injectedProperties = new ArrayList<>();

    public GenerationContext(TypeElement activityClassElement, ProjectSniffer projectSniffer)
            throws InvalidManifestException {
        activityClassName = activityClassElement.getQualifiedName().toString();
        layoutBuilderClassName = activityClassName + "LayoutBuilderDrone";
        activityPackage = activityClassName.substring(0, activityClassName.lastIndexOf('.'));
        layoutBuilderSimpleClassName = getSimpleClassName(layoutBuilderClassName);

        Manifest manifest = projectSniffer.findManifestFile();
        applicationPackage = manifest.getApplicationPackage();
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

    public String getApplicationPackage() {
        return applicationPackage;
    }

    public String getLayoutBuilderSimpleClassName() {
        return layoutBuilderSimpleClassName;
    }

    public List<InjectedProperty> getInjectedProperties() {
        return injectedProperties;
    }

    public void addInjectedProperty(String type, String name) {
        injectedProperties.add(new InjectedProperty(type, name));
    }

    private String getSimpleClassName(String screenClassName) {
        int lastDot = screenClassName.lastIndexOf('.');
        return screenClassName.substring((lastDot == -1 ? 0 : lastDot) + 1);
    }
}
