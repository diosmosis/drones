package com.flarestar.drones.mvw.processing;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.base.inject.GuiceFactoryDelegate;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.function.FunctionDefinition;
import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.model.scope.ScopeDefinition;
import com.flarestar.drones.mvw.processing.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.processing.renderables.DirectiveTreeRoot;
import com.flarestar.drones.mvw.processing.renderables.LayoutBuilder;
import com.flarestar.drones.mvw.processing.renderables.TemplateFunctionProxyCode;
import com.flarestar.drones.mvw.processing.renderables.makeview.DirectiveMakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.processing.renderables.makeview.ViewCreationCode;
import com.flarestar.drones.mvw.processing.renderables.scope.*;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.processing.writer.ScopePropertyValueDeducer;
import com.flarestar.drones.views.viewgroups.BaseDroneViewGroup;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * TODO
 */
public class RenderableFactory {

    private FunctionSniffer functionSniffer;
    private IsolateDirectiveProcessor isolateDirectiveProcessor;
    private GuiceFactoryDelegate factoryDelegate;
    private ScopePropertyValueDeducer scopePropertyValueDeducer;
    private TypeInferer typeInferer;

    @Inject
    public RenderableFactory(FunctionSniffer functionSniffer, IsolateDirectiveProcessor isolateDirectiveProcessor,
                             GuiceFactoryDelegate factoryDelegate, ScopePropertyValueDeducer scopePropertyValueDeducer,
                             TypeInferer typeInferer) {
        this.functionSniffer = functionSniffer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
        this.factoryDelegate = factoryDelegate;
        this.scopePropertyValueDeducer = scopePropertyValueDeducer;
        this.typeInferer = typeInferer;
    }

    public LayoutBuilder createLayoutBuilder(ActivityGenerationContext context, ViewNode layoutRoot) {
        List<TemplateFunctionProxyCode> functions = collectUserFunctionTemplates();
        List<DirectiveTreeRoot> directiveTreeRoots = getUsedDirectiveTrees(layoutRoot);
        Set<ScopeClassDefinition> scopeDefinitions = collectScopeDefinitions(layoutRoot, directiveTreeRoots);
        MakeViewMethod makeViewMethod = createMakeViewMethod(layoutRoot, null);

        return new LayoutBuilder(context, layoutRoot.id, makeViewMethod, scopeDefinitions, functions, directiveTreeRoots);
    }

    private MakeViewMethod createMakeViewMethod(ViewNode view, final Directive rootDirective) {
        ViewFactory viewFactory = createViewFactoryRenderable(view, rootDirective);

        List<MakeViewMethod> childRenderables = Lists.newArrayList(Iterables.transform(view.children,
            new Function<ViewNode, MakeViewMethod>() {
                @Nonnull
                @Override
                public MakeViewMethod apply(@Nonnull ViewNode child) {
                    return createMakeViewMethod(child, rootDirective);
                }
            }
        ));

        String viewId = view.id;

        com.flarestar.drones.mvw.model.scope.ScopeDefinition parentScope = null;
        List<WatcherDefinition> parentScopeWatchers = null;

        boolean hasParent = view.parent != null;
        if (hasParent) {
            parentScope = view.parent.scopeDefinition;
            parentScopeWatchers = view.getParentScopeDirectiveWatchers();
        }

        return new MakeViewMethod(viewId, rootDirective, viewFactory, childRenderables, view.isIsolateDirectiveRoot(),
            hasParent, parentScope, parentScopeWatchers);
    }

    private ViewFactory createViewFactoryRenderable(ViewNode view, Directive rootDirective) {
        MakeViewBody body = createMakeViewBodyRenderable(view, rootDirective);

        com.flarestar.drones.mvw.model.ViewFactory viewFactoryModel = view.getViewFactory();
        Class<? extends ViewFactory> viewFactoryType = viewFactoryModel.getRenderableClass();

        return factoryDelegate.make(viewFactoryType, body, viewFactoryModel);
    }

    private MakeViewBody createMakeViewBodyRenderable(ViewNode view, Directive rootDirective) {
        boolean isDynamicViewGroup = typeInferer.isDynamicViewGroup(view);
        boolean isScopeViewGroup = view.getViewClassName() != null
            && typeInferer.isAssignable(view.getViewClassName(), BaseDroneViewGroup.class.getName());
        boolean hasTransclude = view.hasTransclude() && rootDirective != null && rootDirective.hasTransclude();
        boolean hasOwnScope = view.hasScope();

        String elementText = view.element.ownText();
        Collection<ScopeEventListener> scopeEventListeners = view.scopeDefinition.events;
        Collection<String> childViewIds = Collections2.transform(view.children, new Function<ViewNode, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ViewNode viewNode) {
                return viewNode.id;
            }
        });

        ScopeCreationCode scopeCreationCode = makeScopeCreationCode(view, rootDirective);
        ViewCreationCode viewCreationCode = new ViewCreationCode(view.getViewClassName(), view.viewProperties);

        if (view.isUsingIsolateDirective()) {
            Collection<ScopePropertyRenderable> scopePropertyRenderables = makeScopePropertyRenderables(
                view.scopeDefinition, view.isolateDirective.getScopeProperties());
            String isolateDirectiveName = view.isolateDirective.getDirectiveName();

            return new DirectiveMakeViewBody(
                viewCreationCode, elementText, scopeEventListeners, childViewIds, isDynamicViewGroup, isScopeViewGroup,
                hasTransclude, hasOwnScope, scopeCreationCode, view.getThisScopeDirectiveWatchers(),
                view.hasTranscludeDirective(), isolateDirectiveName, scopePropertyRenderables
            );
        } else {
            return new MakeViewBody(viewCreationCode, elementText, scopeEventListeners, childViewIds,
                isDynamicViewGroup, isScopeViewGroup, hasTransclude, hasOwnScope, scopeCreationCode,
                view.getThisScopeDirectiveWatchers());
        }
    }

    protected static ScopeCreationCode makeScopeCreationCode(ViewNode view, Directive directiveRoot) {
        boolean isInMakeDirectiveMethod = view.parent == null && directiveRoot != null;
        return new ScopeCreationCode(view.scopeDefinition, isInMakeDirectiveMethod, view.hasIsolateDirective());
    }

    private List<TemplateFunctionProxyCode> collectUserFunctionTemplates() {
        return Lists.newArrayList(Iterables.transform(functionSniffer.getUserFunctions(),
            new Function<FunctionDefinition, TemplateFunctionProxyCode>() {
                @Nullable
                @Override
                public TemplateFunctionProxyCode apply(@Nullable FunctionDefinition functionDefinition) {
                    return new TemplateFunctionProxyCode(functionDefinition);
                }
            })
        );
    }

    public List<DirectiveTreeRoot> getUsedDirectiveTrees(ViewNode rootView) {
        final Set<Class<? extends Directive>> directiveClassesFound = new HashSet<>();
        final List<DirectiveTreeRoot> result = new ArrayList<>();

        rootView.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                for (Directive directive : node.directives) {
                    if (!directive.isIsolateDirective() || directiveClassesFound.contains(directive.getClass())) {
                        continue;
                    }

                    ViewNode directiveTree = isolateDirectiveProcessor.getDirectiveTree(directive.getClass());
                    MakeViewMethod method = createMakeViewMethod(directiveTree, directive);
                    result.add(new DirectiveTreeRoot(directive, directiveTree, method));

                    directiveClassesFound.add(directive.getClass());
                }
            }
        });

        return result;
    }

    private Set<ScopeClassDefinition> collectScopeDefinitions(ViewNode tree, List<DirectiveTreeRoot> isolateDirectiveTrees) {
        final Set<ScopeClassDefinition> definitions = new HashSet<>();
        final ViewNode.Visitor scopeDefinitionCollector = new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasScope()) {
                    definitions.add(createScopeDefinition(node.scopeDefinition));
                }
            }
        };

        tree.visit(scopeDefinitionCollector);
        for (DirectiveTreeRoot isolateDirectiveTree : isolateDirectiveTrees) {
            isolateDirectiveTree.getViewNode().visit(scopeDefinitionCollector);
        }

        return definitions;
    }

    private ScopeClassDefinition createScopeDefinition(ScopeDefinition definition) {
        String parentScopeClassName = definition.getParentScope() == null ? null : definition.getParentScope().getScopeClassName();
        String isolateDirectiveName = definition.getOwner().hasIsolateDirective()
            ? definition.getOwner().isolateDirective.getDirectiveName() : null;

        Collection<ScopePropertyRenderable> ownScopeProperties = makeScopePropertyRenderables(
            definition, definition.ownProperties().values());
        Collection<ScopePropertyRenderable> inheritedProperties = makeScopePropertyRenderables(
            definition, definition.inheritedProperties().values());

        Collection<ScopePropertyRenderable> isolateScopeProperties = null;
        if (isolateDirectiveName != null) {
            isolateScopeProperties = makeScopePropertyRenderables(
                definition, definition.getOwner().isolateDirective.getScopeProperties());
        }

        return new ScopeClassDefinition(
            definition.getOwner().isDirectiveRoot,
            definition.getScopeClassName(),
            parentScopeClassName,
            isolateDirectiveName,
            ownScopeProperties,
            inheritedProperties,
            isolateScopeProperties
        );
    }

    private Collection<ScopePropertyRenderable> makeScopePropertyRenderables(final ScopeDefinition definition,
                                                                             Collection<Property> properties) {
        if (properties == null) {
            return null;
        }

        return Collections2.transform(properties, new Function<Property, ScopePropertyRenderable>() {
            @Nullable
            @Override
            public ScopePropertyRenderable apply(@Nullable Property property) {
                return makeScopePropertyRenderable(definition, property);
            }
        });
    }

    private ScopePropertyRenderable makeScopePropertyRenderable(ScopeDefinition definition, Property property) {
        Renderable initialValue = scopePropertyValueDeducer.getInitialValueRenderable(property, definition.getOwner());
        return new ScopePropertyRenderable(
            property.type,
            property.name,
            property.hasBidirectionalBinding(),
            property.canInitializeInScopeConstructor(definition.getOwner().isDirectiveRoot),
            initialValue
        );
    }
}
