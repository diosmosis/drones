package com.flarestar.drones.mvw.writer;

import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
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
    private final ScopePropertyValueDeducer scopePropertyValueDeducer;
    private final FunctionSniffer functionSniffer;

    @Inject
    public LayoutBuilderWriter(StyleProcessor styleProcessor, Interpolator interpolator, TypeInferer typeInferer,
                               IsolateDirectiveProcessor isolateDirectiveProcessor,
                               ScopePropertyValueDeducer scopePropertyValueDeducer, FunctionSniffer functionSniffer,
                               JtwigConfiguration jtwigConfig) {
        Loader.Resource resource = new ClasspathLoader.ClasspathResource("templates/LayoutBuilder.twig");
        template = new JtwigTemplate(resource, jtwigConfig);

        this.styleProcessor = styleProcessor;
        this.interpolator = interpolator;
        this.typeInferer = typeInferer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
        this.scopePropertyValueDeducer = scopePropertyValueDeducer;
        this.functionSniffer = functionSniffer;
    }

    public void writeLayoutBuilder(ActivityGenerationContext context, ViewNode tree, OutputStream output)
            throws JtwigException, LayoutFileException, InvalidUserFunctionClass {
        JtwigModelMap model = new JtwigModelMap();
        addServicesToModel(model);

        model.add("generationContext", context);
        model.add("rootView", tree);

        Map<Directive, ViewNode> isolateDirectiveTrees = getIsolateDirectiveTrees(context, tree);
        model.add("isolateDirectiveTrees", isolateDirectiveTrees);
        model.add("scopeDefinitions", getScopeDefinitions(tree, isolateDirectiveTrees.values()));
        model.add("userFunctions", functionSniffer.detectUserFunctions());

        model.add("package", context.getActivityPackage());
        model.add("applicationPackage", context.getApplicationPackage());
        model.add("className", context.getLayoutBuilderSimpleClassName());
        model.add("screenClassName", context.getActivityClassName());
        model.add("injectedProperties", context.getInjectedProperties());

        String rendered = template.render(model);
        rendered = rendered.replaceAll("\\n[\\s\\n]+\\n", "\n\n");
        try {
            output.write(rendered.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new LayoutFileException("Unable to write to output stream.", e);
        }
    }

    private void addServicesToModel(JtwigModelMap model) {
        model.add("styleProcessor", styleProcessor);
        model.add("interpolator", interpolator);
        model.add("typeInferer", typeInferer);
        model.add("scopePropertyValueDeducer", scopePropertyValueDeducer);
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

                    try {
                        result.put(directive, isolateDirectiveProcessor.getDirectiveTree(context, directive.getClass()));
                    } catch (LayoutFileException e) {
                        throw new RuntimeException(e); // TODO: shouldn't need to handle this here
                    }
                    directiveClassesFound.add(directive.getClass());
                }
            }
        });

        return result;
    }

    public Set<ScopeDefinition> getScopeDefinitions(ViewNode tree, Collection<ViewNode> isolateDirectiveTrees) {
        final Set<ScopeDefinition> definitions = new HashSet<>();
        final ViewNode.Visitor scopeDefinitionCollector = new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasScope()) {
                    definitions.add(node.scopeDefinition);
                }
            }
        };

        tree.visit(scopeDefinitionCollector);
        for (ViewNode isolateDirectiveTree : isolateDirectiveTrees) {
            isolateDirectiveTree.visit(scopeDefinitionCollector);
        }

        return definitions;
    }
}
