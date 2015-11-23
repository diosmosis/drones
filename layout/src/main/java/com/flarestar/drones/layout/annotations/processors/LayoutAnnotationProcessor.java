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
import com.flarestar.drones.layout.DroneModule;
import com.flarestar.drones.layout.LayoutBuilderGenerator;
import com.flarestar.drones.layout.android.Manifest;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeFound;
import com.flarestar.drones.layout.android.exceptions.ManifestCannotBeParsed;
import com.flarestar.drones.layout.annotations.Layout;
import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.parser.LayoutProcessor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.writer.LayoutBuilderWriter;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * TODO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.flarestar.drones.layout.annotations.Layout"})
public class LayoutAnnotationProcessor extends AbstractProcessor {

    private Injector injector;
    private LayoutBuilderGenerator layoutBuilderGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        TypeInferer.createInstance(processingEnv);

        DroneModule module = new DroneModule(processingEnv);
        injector = Guice.createInjector(module);
        layoutBuilderGenerator = injector.getInstance(LayoutBuilderGenerator.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Start processing of @Layout");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Layout.class);
        for (Element element : elements) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found element '" + element.getSimpleName()
                + "' with @Layout.");

            if (!layoutBuilderGenerator.canGenerateFor(element)) {
                continue;
            }

            layoutBuilderGenerator.generateLayoutBuilderFor((TypeElement) element);
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Finish processing of @Layout");

        return true;
    }
}
