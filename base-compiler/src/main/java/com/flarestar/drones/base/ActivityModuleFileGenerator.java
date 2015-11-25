package com.flarestar.drones.base;

import com.google.inject.Singleton;
import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.exception.JtwigException;
import org.jtwig.loader.Loader;
import org.jtwig.loader.impl.ClasspathLoader;

import javax.lang.model.element.TypeElement;
import java.io.OutputStream;

@Singleton
public class ActivityModuleFileGenerator extends BaseFileGenerator {

    public String getGeneratedClassName(TypeElement activityClassElement) {
        return activityClassElement.getQualifiedName().toString() + "ActivityModule";
    }

    public void generate(TypeElement activityClassElement, OutputStream output) throws JtwigException {
        JtwigModelMap model = getModel(activityClassElement);
        renderTemplate(model, output);
    }

    protected JtwigTemplate makeTemplate() {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/ActivityModule.twig");
        return new JtwigTemplate(resource, jtwigConfig);
    }
}
