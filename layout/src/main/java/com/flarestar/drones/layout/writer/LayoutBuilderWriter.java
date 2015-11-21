package com.flarestar.drones.layout.writer;

import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.StyleProcessor;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;
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

    public LayoutBuilderWriter() {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/LayoutBuilder.twig");
        JtwigConfiguration jtwigConfig = JtwigConfigurationBuilder.newConfiguration().build();
        template = new JtwigTemplate(resource, jtwigConfig);
    }

    public void writeLayoutBuilder(String screenClassName, String layoutBuilderClassName,
                                   String applicationPackage, ViewNode tree, OutputStream output)
            throws JtwigException, LayoutFileException {
        JtwigModelMap model = new JtwigModelMap();
        model.add("styleProcessor", new StyleProcessor());
        model.add("interpolator", new Interpolator());

        model.add("rootView", tree);
        model.add("package", getPackageFromClassName(layoutBuilderClassName));
        model.add("applicationPackage", applicationPackage);
        model.add("className", getSimpleClassName(layoutBuilderClassName));
        model.add("screenClassName", screenClassName);

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

    private void collectScreenViews(List<ViewNode> screenViews, ViewNode node) {
        for (ViewNode child : node.children) {
            collectScreenViews(screenViews, child);
        }

        screenViews.add(node);
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

    private String getPackageFromClassName(String screenClassName) {
        int lastDot = screenClassName.lastIndexOf('.');
        return screenClassName.substring(0, lastDot == -1 ? screenClassName.length() : lastDot);
    }

    private String getSimpleClassName(String screenClassName) {
        int lastDot = screenClassName.lastIndexOf('.');
        return screenClassName.substring((lastDot == -1 ? 0 : lastDot) + 1);
    }
}
