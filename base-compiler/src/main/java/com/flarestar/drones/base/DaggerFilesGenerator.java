package com.flarestar.drones.base;

import com.flarestar.drones.base.generation.Generator;
import com.flarestar.drones.base.renderables.ActivityComponent;
import com.flarestar.drones.base.renderables.ActivityModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jtwig.exception.JtwigException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Singleton
public class DaggerFilesGenerator {

    private ProcessingEnvironment processingEnvironment;
    private TypeMirror screenType;
    private Generator generator;
    private ScreenDroneSniffer screenDroneSniffer;

    @Inject
    DaggerFilesGenerator(ProcessingEnvironment processingEnvironment,
                         Generator generator, ScreenDroneSniffer screenDroneSniffer) {
        this.processingEnvironment = processingEnvironment;
        this.generator = generator;
        this.screenDroneSniffer = screenDroneSniffer;

        TypeElement screenTypeElement = processingEnvironment.getElementUtils().getTypeElement(BaseScreen.class.getName());
        if (screenTypeElement == null) {
            throw new RuntimeException("Cannot find BaseScreen TypeElement, is the base drones jar on the classpath?");
        }
        screenType = screenTypeElement.asType();
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
                    + "annotated w/ @Screen");

            return false;
        }

        if (!processingEnvironment.getTypeUtils().isAssignable(element.asType(), screenType)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Element '" + element.getSimpleName() + "' must derive from BaseScreen in order to be used "
                    + "w/ @Screen.");

            return false;
        }

        return true;
    }

    public void generateDaggerFilesFor(TypeElement activityClassElement) {
        List<ScreenDroneSniffer.DroneInformation> drones = screenDroneSniffer.getDroneInformationList(activityClassElement);

        ActivityModule module = new ActivityModule(activityClassElement);
        ActivityComponent component = new ActivityComponent(activityClassElement, module, drones);

        try {
            generator.renderClass(module);
            generator.renderClass(component);
        } catch (IOException | JtwigException e) {
            throw new RuntimeException(e);
        }
    }
}
