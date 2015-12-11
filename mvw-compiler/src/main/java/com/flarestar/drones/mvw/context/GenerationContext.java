package com.flarestar.drones.mvw.context;

import com.flarestar.drones.mvw.android.Manifest;
import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.compilerutilities.ProjectSniffer;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GenerationContext {

    public static class InjectedProperty {
        public final String type;
        public final String name;

        public InjectedProperty(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private final String applicationPackage;

    private List<InjectedProperty> injectedProperties = new ArrayList<>();

    public GenerationContext(ProjectSniffer projectSniffer)
            throws InvalidManifestException {
        Manifest manifest = projectSniffer.findManifestFile();
        applicationPackage = manifest.getApplicationPackage();
    }

    public String getApplicationPackage() {
        return applicationPackage;
    }

    public abstract String getLayoutBuilderSimpleClassName();

    public List<InjectedProperty> getInjectedProperties() {
        return injectedProperties;
    }

    public void addInjectedProperty(String type, String name) {
        injectedProperties.add(new InjectedProperty(type, name));
    }
}
