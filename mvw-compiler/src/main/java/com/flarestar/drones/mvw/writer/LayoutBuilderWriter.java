package com.flarestar.drones.mvw.writer;

import com.flarestar.drones.mvw.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.StyleProcessor;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;
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
    private final TypeInferer typeInferer;
    private final IsolateDirectiveProcessor isolateDirectiveProcessor;

    @Inject
    public LayoutBuilderWriter(StyleProcessor styleProcessor, Interpolator interpolator, TypeInferer typeInferer,
                               IsolateDirectiveProcessor isolateDirectiveProcessor) {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/LayoutBuilder.twig");
        JtwigConfiguration jtwigConfig = JtwigConfigurationBuilder.newConfiguration().build();
        template = new JtwigTemplate(resource, jtwigConfig);

        this.styleProcessor = styleProcessor;
        this.interpolator = interpolator;
        this.typeInferer = typeInferer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
    }

    public void writeLayoutBuilder(ActivityGenerationContext context, ViewNode tree, OutputStream output)
            throws JtwigException, LayoutFileException {
        JtwigModelMap model = new JtwigModelMap();
        model.add("styleProcessor", styleProcessor);
        model.add("interpolator", interpolator);
        model.add("typeInferer", typeInferer);

        model.add("generationContext", context);
        model.add("rootView", tree);
        model.add("isolateDirectiveTrees", getIsolateDirectiveTrees(context, tree));

        model.add("package", context.getActivityPackage());
        model.add("applicationPackage", context.getApplicationPackage());
        model.add("className", context.getLayoutBuilderSimpleClassName());
        model.add("screenClassName", context.getActivityClassName());
        model.add("injectedProperties", context.getInjectedProperties());

        final Set<ScopeDefinition> definitions = new HashSet<>();
        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasScope()) {
                    definitions.add(node.scopeDefinition);
                }
            }
        });
        model.add("scopeDefinitions", definitions);

        String rendered = template.render(model);
        rendered = rendered.replaceAll("\\n[\\s\\n]+\\n", "\n\n");
        try {
            output.write(rendered.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new LayoutFileException("Unable to write to output stream.", e);
        }
    }

    private Map<Directive, ViewNode> getIsolateDirectiveTrees(final ActivityGenerationContext context, ViewNode tree) {
        final Set<Class<? extends Directive>> directiveClassesFound = new HashSet<>();
        final Map<Directive, ViewNode> result = new HashMap<>();

        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                for (Directive directive : node.directives) {
                    if (!directive.isIsolateDirective() || directiveClassesFound.contains(directive.getClass())) {
                        continue;
                    }

                    result.put(directive, isolateDirectiveProcessor.getDirectiveTree(context, directive.getClass()));
                    directiveClassesFound.add(directive.getClass());
                }
            }
        });

        return result;
    }
}
