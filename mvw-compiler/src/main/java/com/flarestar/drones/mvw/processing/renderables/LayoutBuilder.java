package com.flarestar.drones.mvw.processing.renderables;

import com.flarestar.drones.base.generation.ClassRenderable;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeClassDefinition;

import java.util.*;

/**
 * TODO
 */
public class LayoutBuilder implements ClassRenderable {
    private ActivityGenerationContext context;
    private List<DirectiveTreeRoot> isolateDirectiveTrees;
    private Collection<ScopeClassDefinition> scopeDefinitions;
    private MakeViewMethod rootMakeViewMethod;
    private List<TemplateFunctionProxyCode> userFunctions;
    private String rootViewId;

    public LayoutBuilder(ActivityGenerationContext context, String rootViewId, MakeViewMethod rootMakeViewMethod,
                         Collection<ScopeClassDefinition> scopeDefinitions, List<TemplateFunctionProxyCode> userFunctions,
                         List<DirectiveTreeRoot> isolateDirectiveTrees) {
        this.context = context;
        this.rootViewId = rootViewId;
        this.rootMakeViewMethod = rootMakeViewMethod;
        this.isolateDirectiveTrees = isolateDirectiveTrees;
        this.scopeDefinitions = scopeDefinitions;
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

    public Collection<ScopeClassDefinition> getScopeDefinitions() {
        return scopeDefinitions;
    }

    public MakeViewMethod getRootMakeViewMethod() {
        return rootMakeViewMethod;
    }

    public List<TemplateFunctionProxyCode> getUserFunctions() {
        return userFunctions;
    }

    public String getRootViewId() {
        return rootViewId;
    }
}
