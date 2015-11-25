package com.flarestar.drones.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jtwig.exception.JtwigException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;

@Singleton
public class DaggerFilesGenerator {

    private ProcessingEnvironment processingEnvironment;
    private ActivityModuleFileGenerator activityModuleFileGenerator;
    private ComponentFileGenerator componentFileGenerator;
    private TypeMirror screenType;

    @Inject
    DaggerFilesGenerator(ProcessingEnvironment processingEnvironment,
                         ActivityModuleFileGenerator activityModuleFileGenerator,
                         ComponentFileGenerator componentFileGenerator) {
        this.processingEnvironment = processingEnvironment;
        this.activityModuleFileGenerator = activityModuleFileGenerator;
        this.componentFileGenerator = componentFileGenerator;

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
        String activityClassName = activityClassElement.getQualifiedName().toString();

        // generate ..ActivityModule.java file
        String activityModuleClassName = activityModuleFileGenerator.getGeneratedClassName(activityClassElement);
        try (OutputStream output = openNewSourceFile(activityClassName, activityModuleClassName)) {
            activityModuleFileGenerator.generate(activityClassElement, output);
        } catch (IOException | JtwigException e) {
            throw new RuntimeException(e);
        }

        // generate ..ActivityComponent.java file
        String activityComponentClassName = componentFileGenerator.getGeneratedClassName(activityClassElement);
        try (OutputStream output = openNewSourceFile(activityClassName, activityComponentClassName)) {
            componentFileGenerator.generate(activityClassElement, output);
        } catch (IOException | JtwigException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputStream openNewSourceFile(String activityClassName, String newClassName) {
        JavaFileObject newObject;
        try {
            newObject = processingEnvironment.getFiler().createSourceFile(newClassName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '" + activityClassName + "'.", e);
        }

        try {
            return newObject.openOutputStream();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open new class file '" + newClassName + ".", e);
        }
    }
}
