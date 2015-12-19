package com.flarestar.drones.mvw.processing.renderables;

import com.flarestar.drones.base.generation.ClassRenderable;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.function.FunctionDefinition;
import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.flarestar.drones.mvw.processing.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeDefinition;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
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
    private MakeViewMethod rootMakeViewMethod;
    private List<TemplateFunctionProxyCode> userFunctions;

    public LayoutBuilder(ActivityGenerationContext context, ViewNode rootView, MakeViewMethod rootMakeViewMethod,
                         List<TemplateFunctionProxyCode> userFunctions, List<DirectiveTreeRoot> isolateDirectiveTrees) {
        this.context = context;
        this.rootView = rootView;
        this.rootMakeViewMethod = rootMakeViewMethod;
        this.isolateDirectiveTrees = isolateDirectiveTrees;
        this.scopeDefinitions = collectScopeDefinitions(rootView);
        this.userFunctions = userFunctions;
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
}
