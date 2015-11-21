package com.flarestar.drones.layout.annotations.processors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;

import com.flarestar.drones.base.Screen;
import com.flarestar.drones.layout.android.Manifest;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeFound;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeParsed;
import com.flarestar.drones.layout.annotations.Layout;
import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.parser.LayoutProcessor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.writer.LayoutBuilderWriter;

/**
 * TODO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.flarestar.drones.layout.annotations.Layout"})
public class LayoutAnnotationProcessor extends AbstractProcessor {

    private TypeMirror screenType;

    private LayoutProcessor xmlProcessor;
    private LayoutBuilderWriter layoutBuilderWriter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        xmlProcessor = new LayoutProcessor();
        layoutBuilderWriter = new LayoutBuilderWriter();

        TypeElement screenTypeElement = processingEnv.getElementUtils().getTypeElement(Screen.class.getName());
        if (screenTypeElement == null) {
            throw new RuntimeException("Cannot find the '" + Screen.class.getName() + "' type, is the base drone on the classpath?");
        }

        screenType = screenTypeElement.asType();

        TypeInferer.createInstance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Start processing of @Layout");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Layout.class);
        for (Element element : elements) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Found element '" + element.getSimpleName() + "' with @Layout.");

            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "Element '" + element.getSimpleName() + "' is not a class, only class elements should be "
                        + "annotated w/ @Layout");

                continue;
            }

            if (!processingEnv.getTypeUtils().isAssignable(element.asType(), screenType)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "Element '" + element.getSimpleName() + "' must derive from Screen in order to be used "
                        + "w/ @Layout.");

                continue;
            }

            this.generateLayoutBuilderFor((TypeElement) element);
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Finish processing of @Layout");

        return true;
    }

    // TODO: need to introduce DI before doing more additions
    private void generateLayoutBuilderFor(TypeElement element) {
        String screenClassName = element.getQualifiedName().toString();
        String layoutBuilderClassName = screenClassName + "LayoutBuilderDrone";
        String screenPackage = screenClassName.substring(0, screenClassName.lastIndexOf('.'));

        TypeInferer.getInstance().setBasePackage(screenPackage);

        Layout annotation = element.getAnnotation(Layout.class);
        String layoutFilePath = annotation.value();
        String stylesheetFilePath = annotation.stylesheet();

        FileObject layoutFileObject;
        try {
            layoutFileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", layoutFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: cannot open " + layoutFilePath);
        }

        FileObject stylesheetFileObject = null;
        if (!stylesheetFilePath.isEmpty()) {
            try {
                stylesheetFileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", stylesheetFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error: cannot open " + stylesheetFilePath);
            }
        }

        ViewNode tree;
        try (InputStream layoutInput = layoutFileObject.openInputStream();
             InputStream stylesheetInput = stylesheetFileObject == null ? null : stylesheetFileObject.openInputStream()
        ) {
            tree = xmlProcessor.createViewTree(layoutInput, stylesheetInput);
        } catch (LayoutFileException e) {
            throw new RuntimeException("Layout file " + layoutFilePath + " is malformed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resources/" + annotation.value() + " layout file.", e);
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating '" + layoutBuilderClassName + "'.");

        JavaFileObject newObject;
        try {
            newObject = processingEnv.getFiler().createSourceFile(layoutBuilderClassName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '" + screenClassName + "'.", e);
        }

        Manifest manifest = null;
        try {
            manifest = Manifest.findManifestFile(layoutFileObject.toUri().getPath());
        } catch (ManifestCannotBeFound | ManifestCannotBeParsed ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        try (OutputStream output = newObject.openOutputStream()) {
            layoutBuilderWriter.writeLayoutBuilder(screenClassName, layoutBuilderClassName,
                manifest.getApplicationPackage(), tree, output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '"+ screenClassName + "'.", e);
        }
    }
}
