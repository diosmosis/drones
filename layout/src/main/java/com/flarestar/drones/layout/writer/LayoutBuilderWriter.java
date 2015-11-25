package com.flarestar.drones.layout.writer;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.StyleProcessor;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;
import com.google.inject.Inject;
import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;
import org.jtwig.exception.JtwigException;
import org.jtwig.loader.Loader;
import org.jtwig.loader.impl.ClasspathLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * TODO
 */
public class LayoutBuilderWriter {

    private final JtwigTemplate template;
    private final StyleProcessor styleProcessor;
    private final Interpolator interpolator;

    @Inject
    public LayoutBuilderWriter(StyleProcessor styleProcessor, Interpolator interpolator) {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/LayoutBuilder.twig");
        JtwigConfiguration jtwigConfig = JtwigConfigurationBuilder.newConfiguration().build();
        template = new JtwigTemplate(resource, jtwigConfig);

        this.styleProcessor = styleProcessor;
        this.interpolator = interpolator;
    }

    public void writeLayoutBuilder(GenerationContext context, ViewNode tree, OutputStream output)
            throws JtwigException, LayoutFileException {
        JtwigModelMap model = new JtwigModelMap();
        model.add("styleProcessor", styleProcessor);
        model.add("interpolator", interpolator);

        model.add("generationContext", context);
        model.add("rootView", tree);
        model.add("package", context.getActivityPackage());
        model.add("applicationPackage", context.getApplicationPackage());
        model.add("className", context.getLayoutBuilderSimpleClassName());
        model.add("screenClassName", context.getActivityClassName());
        model.add("injectedProperties", context.getInjectedProperties());

        // TODO: viewnode needs a visit method
        Set<ScopeDefinition> definitions = new HashSet<>();
        collectUniqueScopeDefinitions(definitions, tree);
        model.add("scopeDefinitions", definitions);

        String rendered = template.render(model);
        rendered = rendered.replaceAll("\\n[\\s\\n]+\\n", "\n\n");
        try {
            output.write(rendered.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new LayoutFileException("Unable to write to output stream.", e);
        }
    }

    private void collectUniqueScopeDefinitions(Set<ScopeDefinition> definitions, ViewNode node)
            throws LayoutFileException {
        if (node.hasScope()) {
            definitions.add(node.getScopeDefinition());
        }

        for (ViewNode child : node.children) {
            collectUniqueScopeDefinitions(definitions, child);
        }
    }
}
