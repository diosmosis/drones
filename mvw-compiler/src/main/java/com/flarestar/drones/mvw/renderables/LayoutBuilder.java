package com.flarestar.drones.mvw.renderables;

import com.flarestar.drones.base.generation.ClassRenderable;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.function.FunctionDefinition;
import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.flarestar.drones.mvw.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.renderables.scope.ScopeDefinition;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.*;

/**
 * TODO
 */
public class LayoutBuilder implements ClassRenderable {
    private ActivityGenerationContext context;
    private List<DirectiveTreeRoot> isolateDirectiveTrees;
    private Set<ScopeDefinition> scopeDefinitions;
    private ViewNode rootView;
    private FunctionSniffer functionSniffer;
    private IsolateDirectiveProcessor isolateDirectiveProcessor;
    private MakeViewMethod rootMakeViewMethod;
    private List<TemplateFunctionProxyCode> userFunctions;

    public LayoutBuilder(ActivityGenerationContext context, ViewNode rootView, FunctionSniffer functionSniffer,
                         IsolateDirectiveProcessor isolateDirectiveProcessor)
            throws LayoutFileException, InvalidUserFunctionClass {
        this.context = context;
        this.rootView = rootView;
        this.functionSniffer = functionSniffer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
        this.rootMakeViewMethod = new MakeViewMethod(rootView, null);
        this.isolateDirectiveTrees = collectIsolateDirectiveTrees(context, rootView);
        this.scopeDefinitions = collectScopeDefinitions(rootView);
        this.userFunctions = collectUserFunctionTemplates();
    }

    @Override
    public String getFullGeneratedClassName() {
        return context.getLayoutBuilderClassName();
    }

    @Override
    public String getTemplate() {
        return "templates/LayoutBuilder.twig";
    }

    @Override
    public String getModelAttribute() {
        return "builder";
    }

    public ActivityGenerationContext getContext() {
        return context;
    }

    public List<GenerationContext.InjectedProperty> getInjectedProperties() {
        return context.getInjectedProperties();
    }

    public List<DirectiveTreeRoot> getIsolateDirectiveTrees() {
        return isolateDirectiveTrees;
    }

    public Set<ScopeDefinition> getScopeDefinitions() {
        return scopeDefinitions;
    }

    public ViewNode getRootView() {
        return rootView;
    }

    public MakeViewMethod getRootMakeViewMethod() {
        return rootMakeViewMethod;
    }

    public List<TemplateFunctionProxyCode> getUserFunctions() {
        return userFunctions;
    }

    private List<DirectiveTreeRoot> collectIsolateDirectiveTrees(final ActivityGenerationContext context, ViewNode tree) {
        final Set<Class<? extends Directive>> directiveClassesFound = new HashSet<>();
        final List<DirectiveTreeRoot> result = new ArrayList<>();

        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                for (Directive directive : node.directives) {
                    if (!directive.isIsolateDirective() || directiveClassesFound.contains(directive.getClass())) {
                        continue;
                    }

                    try {
                        ViewNode directiveTree = isolateDirectiveProcessor.getDirectiveTree(context, directive.getClass());
                        result.add(new DirectiveTreeRoot(directive, directiveTree));
                    } catch (LayoutFileException e) {
                        throw new RuntimeException(e); // TODO: shouldn't need to handle this here
                    }

                    directiveClassesFound.add(directive.getClass());
                }
            }
        });

        return result;
    }

    private Set<ScopeDefinition> collectScopeDefinitions(ViewNode tree) {
        final Set<ScopeDefinition> definitions = new HashSet<>();
        final ViewNode.Visitor scopeDefinitionCollector = new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasScope()) {
                    definitions.add(new ScopeDefinition(node.scopeDefinition));
                }
            }
        };

        tree.visit(scopeDefinitionCollector);
        for (DirectiveTreeRoot isolateDirectiveTree : isolateDirectiveTrees) {
            isolateDirectiveTree.getViewNode().visit(scopeDefinitionCollector);
        }

        return definitions;
    }

    private List<TemplateFunctionProxyCode> collectUserFunctionTemplates() throws InvalidUserFunctionClass {
        return Lists.newArrayList(Iterables.transform(functionSniffer.detectUserFunctions(),
            new Function<FunctionDefinition, TemplateFunctionProxyCode>() {
                @Nullable
                @Override
                public TemplateFunctionProxyCode apply(@Nullable FunctionDefinition functionDefinition) {
                    return new TemplateFunctionProxyCode(functionDefinition);
                }
            })
        );
    }
}
