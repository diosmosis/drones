package com.flarestar.drones.base;

import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;
import org.jtwig.exception.JtwigException;

import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public abstract class BaseFileGenerator {

    protected JtwigConfiguration jtwigConfig = JtwigConfigurationBuilder.newConfiguration().build();
    protected JtwigTemplate template;

    protected void renderTemplate(JtwigModelMap model, OutputStream output) throws JtwigException {
        String rendered = getTemplate().render(model);
        rendered = rendered.replaceAll("\\n[\\s\\n]+\\n", "\n\n");
        try {
            output.write(rendered.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to output stream.", e);
        }
    }

    protected abstract JtwigTemplate makeTemplate();
    public abstract String getGeneratedClassName(TypeElement activityClassElement);

    public JtwigTemplate getTemplate() {
        if (template == null) {
            template = makeTemplate();
        }
        return template;
    }

    protected  JtwigModelMap getModel(TypeElement activityClassElement) {
        String fullModuleClassName = getGeneratedClassName(activityClassElement);

        int lastDot = fullModuleClassName.lastIndexOf('.');
        String modulePackage = fullModuleClassName.substring(0, lastDot == -1 ? fullModuleClassName.length() : lastDot);
        String moduleClassName = fullModuleClassName.substring((lastDot == -1 ? 0 : lastDot) + 1);

        JtwigModelMap model = new JtwigModelMap();
        model.add("package", modulePackage);
        model.add("moduleClassName", moduleClassName);
        return model;
    }
}
