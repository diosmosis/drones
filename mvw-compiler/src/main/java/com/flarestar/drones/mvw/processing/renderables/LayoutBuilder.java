package com.flarestar.drones.mvw.processing.renderables;

import com.flarestar.drones.base.generation.ClassRenderable;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeClassDefinition;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeComponent;

import java.util.*;

/**
 * TODO
 *
 * - create scope component renderable (provides all scope types)
 * - change injected properties to be injected in scope definitions
 */
public class LayoutBuilder implements ClassRenderable {
    private ActivityGenerationContext context;
    private List<DirectiveTreeRoot> isolateDirectiveTrees;
    private Collection<ScopeClassDefinition> scopeDefinitions;
    private MakeViewMethod rootMakeViewMethod;
    private List<TemplateFunctionProxyCode> userFunctions;
    private ScopeComponent scopeComponent;
    private String rootViewId;

    public LayoutBuilder(ActivityGenerationContext context, String rootViewId, MakeViewMethod rootMakeViewMethod,
                         Collection<ScopeClassDefinition> scopeDefinitions, List<TemplateFunctionProxyCode> userFunctions,
                         List<DirectiveTreeRoot> isolateDirectiveTrees, ScopeComponent scopeComponent) {
        this.context = context;
        this.rootViewId = rootViewId;
        this.rootMakeViewMethod = rootMakeViewMethod;
        this.isolateDirectiveTrees = isolateDirectiveTrees;
        this.scopeDefinitions = scopeDefinitions;
        this.userFunctions = userFunctions;
        this.scopeComponent = scopeComponent;
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

    public ScopeComponent getScopeComponent() {
        return scopeComponent;
    }
}
