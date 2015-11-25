package com.flarestar.drones.base.annotations.processors;

import com.flarestar.drones.base.DaggerFilesGenerator;
import com.flarestar.drones.base.DroneModule;
import com.flarestar.drones.base.annotations.Screen;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({"com.flarestar.drones.base.annotations.Screen"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ScreenAnnotationProcessor extends AbstractProcessor {

    private Injector injector;
    private DaggerFilesGenerator daggerFilesGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        DroneModule droneModule = new DroneModule(processingEnv);
        injector = Guice.createInjector(droneModule);
        daggerFilesGenerator = injector.getInstance(DaggerFilesGenerator.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        /* TODO:
           - basic dagger integration, all drones stored in dagger component
             we need to generate the component, module (which provides the activity instance)
           - enhanced dagger integration, any object a user want should be in DI and available
             to drones (ie, controllers)

           (ADVANCED INTEGRATION)
           1. sooo for this, let's make users define their own modules. they put whatever they want in, ie, controllers.
           2. the module is specified to the Screen annotation. the annotation processor passes it on to the generated
              component class.
           3. nowwww, how do drones use these classes? we can't simply add them to a constructor property. **we'll
              use field injection.** we have to mark a property as a injected drone field in Controller.java. should
              work out i think. then we set the scope to the private var.
           4. then we're all done. :)
         */
        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Start processing of @Screen");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Screen.class);
        for (Element element : elements) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found element '" + element.getSimpleName()
                + "' with @Screen.");

            if (!daggerFilesGenerator.canGenerateFor(element)) {
                continue;
            }

            daggerFilesGenerator.generateDaggerFilesFor((TypeElement)element);
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "Finish processing of @Screen");

        return true;
    }
}
