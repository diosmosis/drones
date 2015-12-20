package com.flarestar.drones.mvw.context;

import com.flarestar.drones.mvw.android.Manifest;
import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.compilerutilities.ProjectSniffer;

import java.util.ArrayList;
import java.util.List;

// TODO: merge w/ ActivityGenerationContext
public abstract class GenerationContext {

    private final String applicationPackage;

    public GenerationContext(ProjectSniffer projectSniffer)
            throws InvalidManifestException {
        Manifest manifest = projectSniffer.findManifestFile();
        applicationPackage = manifest.getApplicationPackage();
    }

    public String getApplicationPackage() {
        return applicationPackage;
    }

    public abstract String getLayoutBuilderSimpleClassName();
}
