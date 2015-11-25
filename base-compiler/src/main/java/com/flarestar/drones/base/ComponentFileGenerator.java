package com.flarestar.drones.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.exception.JtwigException;
import org.jtwig.loader.Loader;
import org.jtwig.loader.impl.ClasspathLoader;

import javax.lang.model.element.TypeElement;
import java.io.OutputStream;

@Singleton
public class ComponentFileGenerator extends BaseFileGenerator {

    private ActivityModuleFileGenerator activityModuleFileGenerator;
    private ScreenDroneSniffer screenDroneSniffer;

    @Inject
    public ComponentFileGenerator(ActivityModuleFileGenerator activityModuleFileGenerator,
                                  ScreenDroneSniffer screenDroneSniffer) {
        this.activityModuleFileGenerator = activityModuleFileGenerator;
        this.screenDroneSniffer = screenDroneSniffer;
    }

    public String getGeneratedClassName(TypeElement activityClassElement) {
        return activityClassElement.getQualifiedName().toString() + "ActivityComponent";
    }

    public void generate(TypeElement activityClassElement, OutputStream output) throws JtwigException {
        JtwigModelMap model = getModel(activityClassElement);
        renderTemplate(model, output);
    }

    @Override
    protected JtwigTemplate makeTemplate() {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/ActivityComponent.twig");
        return new JtwigTemplate(resource, jtwigConfig);
    }

    @Override
    protected JtwigModelMap getModel(TypeElement activityClassElement) {
        String activityModuleClass = activityModuleFileGenerator.getGeneratedClassName(activityClassElement);
        String activityModuleClassSimpleName = activityModuleClass.substring(activityModuleClass.lastIndexOf('.') + 1);

        JtwigModelMap model = super.getModel(activityClassElement);
        model.add("activityModuleClass", activityModuleClass);
        model.add("activityModuleClassLc", lcFirst(activityModuleClassSimpleName));
        model.add("drones", screenDroneSniffer.getDroneInformationList(activityClassElement));
        return model;
    }

    private String lcFirst(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
