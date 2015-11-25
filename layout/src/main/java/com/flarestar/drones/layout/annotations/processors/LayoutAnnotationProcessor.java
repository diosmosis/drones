package com.flarestar.drones.layout.annotations.processors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;

import com.flarestar.drones.layout.DroneModule;
import com.flarestar.drones.layout.LayoutBuilderGenerator;
import com.flarestar.drones.layout.annotations.Layout;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * TODO
 *
 * TODO: check that activity is annotated w/ @Screen too. use a base annotation processor in base-compiler?
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.flarestar.drones.layout.annotations.Layout"})
public class LayoutAnnotationProcessor extends AbstractProcessor {

    private Injector injector;
    private LayoutBuilderGenerator layoutBuilderGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        DroneModule module = new DroneModule(processingEnv);
        injector = Guice.createInjector(module);
        layoutBuilderGenerator = injector.getInstance(LayoutBuilderGenerator.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
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
