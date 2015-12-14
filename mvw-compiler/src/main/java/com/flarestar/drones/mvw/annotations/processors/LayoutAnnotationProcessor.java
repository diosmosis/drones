package com.flarestar.drones.mvw.annotations.processors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;

import com.flarestar.drones.mvw.DroneModule;
import com.flarestar.drones.mvw.LayoutBuilderGenerator;
import com.flarestar.drones.mvw.annotations.Layout;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * TODO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.flarestar.drones.mvw.annotations.Layout"})
public class LayoutAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        DroneModule module = new DroneModule(processingEnv, roundEnvironment);

        Injector injector = Guice.createInjector(module);
        LayoutBuilderGenerator layoutBuilderGenerator = injector.getInstance(LayoutBuilderGenerator.class);

        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Start processing of @Layout");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Layout.class);
        for (Element element : elements) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found element '" + element.getSimpleName()
                + "' with @Layout.");

            if (!layoutBuilderGenerator.canGenerateFor(element)) {
                continue;
            }

            layoutBuilderGenerator.generateLayoutBuilderFor((TypeElement) element);
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Finish processing of @Layout");

        return true;
    }
}
