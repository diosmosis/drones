package com.flarestar.drones.layout;

import com.flarestar.drones.base.BaseScreen;
import com.flarestar.drones.layout.android.Manifest;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeFound;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeParsed;
import com.flarestar.drones.layout.annotations.Layout;
import com.flarestar.drones.layout.compilerutilities.ProjectSniffer;
import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.parser.LayoutProcessor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.writer.LayoutBuilderWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.*;
import java.io.*;

/**
 * TODO
 */
@Singleton
public class LayoutBuilderGenerator {

    private LayoutProcessor xmlProcessor;
    private LayoutBuilderWriter layoutBuilderWriter;
    private ProcessingEnvironment processingEnvironment;
    private TypeMirror screenType;
    private ProjectSniffer projectSniffer;
    private TypeInferer typeInferer;

    @Inject
    public LayoutBuilderGenerator(LayoutProcessor xmlProcessor, LayoutBuilderWriter layoutBuilderWriter,
                                  ProcessingEnvironment processingEnvironment, ProjectSniffer projectSniffer,
                                  TypeInferer typeInferer) {
        this.xmlProcessor = xmlProcessor;
        this.layoutBuilderWriter = layoutBuilderWriter;
        this.processingEnvironment = processingEnvironment;
        this.projectSniffer = projectSniffer;
        this.typeInferer = typeInferer;

        try {
            this.screenType = typeInferer.getTypeMirrorFor(BaseScreen.class.getName());
        } catch (IllegalStateException ex) {
            throw new RuntimeException("Cannot find the '" + BaseScreen.class.getName() + "' type, is the base drone on the classpath?", ex);
        }
    }

    public boolean canGenerateFor(Element element) {
        if (!(element instanceof TypeElement)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unexpected state, element '"
                + element.getSimpleName() + "' is not a TypeElement. Skipping.");

            return false;
        }

        if (element.getKind() != ElementKind.CLASS) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Element '" + element.getSimpleName() + "' is not a class, only class elements should be "
                    + "annotated w/ @Layout");

            return false;
        }

        if (!processingEnvironment.getTypeUtils().isAssignable(element.asType(), screenType)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Element '" + element.getSimpleName() + "' must derive from BaseScreen in order to be used "
                    + "w/ @Layout.");

            return false;
        }

        return true;
    }

    public void generateLayoutBuilderFor(TypeElement activityClassElement) {
        String screenClassName = activityClassElement.getQualifiedName().toString();
        String layoutBuilderClassName = screenClassName + "LayoutBuilderDrone";
        String screenPackage = screenClassName.substring(0, screenClassName.lastIndexOf('.'));

        typeInferer.setBasePackage(screenPackage); // TODO: should not have this mutability, but not sure how to remove...

        Layout annotation = activityClassElement.getAnnotation(Layout.class);
        String layoutFilePath = annotation.value();
        String stylesheetFilePath = annotation.stylesheet();

        FileObject layoutFileObject;
        try {
            layoutFileObject = processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", layoutFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: cannot open " + layoutFilePath, e);
        }

        FileObject stylesheetFileObject = null;
        if (!stylesheetFilePath.isEmpty()) {
            try {
                stylesheetFileObject = processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", stylesheetFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error: cannot open " + stylesheetFilePath, e);
            }
        }

        ViewNode tree;
        try (InputStream layoutInput = layoutFileObject.openInputStream();
             InputStream stylesheetInput = stylesheetFileObject == null ? null : stylesheetFileObject.openInputStream()
        ) {
            tree = xmlProcessor.createViewTree(layoutInput, stylesheetInput);
        } catch (LayoutFileException e) {
            throw new RuntimeException("Layout file " + layoutFilePath + " is malformed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + annotation.value() + " layout file.", e);
        }

        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating '" + layoutBuilderClassName + "'.");

        Manifest manifest = null;
        try {
            manifest = projectSniffer.findManifestFile();
        } catch (ManifestCannotBeFound | ManifestCannotBeParsed ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        JavaFileObject newObject;
        try {
            newObject = processingEnvironment.getFiler().createSourceFile(layoutBuilderClassName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '" + screenClassName + "'.", e);
        }

        try (OutputStream output = newObject.openOutputStream()) {
            layoutBuilderWriter.writeLayoutBuilder(screenClassName, layoutBuilderClassName,
                manifest.getApplicationPackage(), tree, output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '"+ screenClassName + "'.", e);
        }
    }
}
