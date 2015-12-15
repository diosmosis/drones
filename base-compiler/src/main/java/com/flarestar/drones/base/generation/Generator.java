package com.flarestar.drones.base.generation;

import com.flarestar.drones.base.renderables.ActivityModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.exception.JtwigException;
import org.jtwig.loader.Loader;
import org.jtwig.loader.impl.ClasspathLoader;
import org.jtwig.render.RenderContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * TODO
 */
public class Generator {

    private Provider<JtwigConfiguration> jtwigConfigProvider;
    private ProcessingEnvironment processingEnvironment;

    @Inject
    public Generator(Provider<JtwigConfiguration> jtwigConfigProvider, ProcessingEnvironment processingEnvironment) {
        this.jtwigConfigProvider = jtwigConfigProvider;
        this.processingEnvironment = processingEnvironment;
    }

    public void render(RenderContext renderContext, Renderable renderable) throws JtwigException, IOException {
        byte[] renderedBytes = renderTemplate(renderable);
        renderContext.write(renderedBytes);
    }

    public void render(Renderable renderable, OutputStream output) throws JtwigException, IOException {
        byte[] renderedBytes = renderTemplate(renderable);
        output.write(renderedBytes);
    }

    private JtwigModelMap getModel(Renderable renderable) {
        JtwigModelMap model = new JtwigModelMap();
        model.add(renderable.getModelAttribute(), renderable);
        return model;
    }

    protected JtwigTemplate getTemplate(Renderable renderable) {
        String template = renderable.getTemplate();
        Loader.Resource resource = new ClasspathLoader.ClasspathResource(template);
        return new JtwigTemplate(resource, jtwigConfigProvider.get());
    }

    public void renderClass(ClassRenderable renderable) throws IOException, JtwigException {
        try (OutputStream output = openNewSourceFile(renderable.getFullGeneratedClassName())) {
            render(renderable, output);
        }
    }

    private OutputStream openNewSourceFile(String newClassName) throws IOException {
        JavaFileObject newObject = processingEnvironment.getFiler().createSourceFile(newClassName);
        return newObject.openOutputStream();
    }

    private byte[] renderTemplate(Renderable renderable) throws JtwigException {
        JtwigTemplate template = getTemplate(renderable);
        JtwigModelMap model = getModel(renderable);

        String rendered = template.render(model);
        rendered = rendered.replaceAll("\\n[\\s\\n]+\\n", "\n\n");

        return rendered.getBytes(Charset.forName("UTF-8"));
    }
}
