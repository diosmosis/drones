package com.flarestar.drones.mvw.processing;

import com.flarestar.drones.base.inject.GuiceFactoryDelegate;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.function.FunctionDefinition;
import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.processing.renderables.DirectiveTreeRoot;
import com.flarestar.drones.mvw.processing.renderables.LayoutBuilder;
import com.flarestar.drones.mvw.processing.renderables.TemplateFunctionProxyCode;
import com.flarestar.drones.mvw.processing.renderables.makeview.DirectiveMakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class RenderableFactory {

    private FunctionSniffer functionSniffer;
    private IsolateDirectiveProcessor isolateDirectiveProcessor;
    private GuiceFactoryDelegate factoryDelegate;

    @Inject
    public RenderableFactory(FunctionSniffer functionSniffer, IsolateDirectiveProcessor isolateDirectiveProcessor,
                             GuiceFactoryDelegate factoryDelegate) {
        this.functionSniffer = functionSniffer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
        this.factoryDelegate = factoryDelegate;
    }

    public LayoutBuilder createLayoutBuilder(ActivityGenerationContext context, ViewNode layoutRoot) {
        List<TemplateFunctionProxyCode> functions = collectUserFunctionTemplates();
        List<DirectiveTreeRoot> directiveTreeRoots = getUsedDirectiveTrees(layoutRoot);

        return new LayoutBuilder(context, layoutRoot, createMakeViewMethod(layoutRoot, null), functions, directiveTreeRoots);
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

        return new MakeViewMethod(view, rootDirective, viewFactory, childRenderables);
    }

    private ViewFactory createViewFactoryRenderable(ViewNode view, Directive rootDirective) {
        MakeViewBody body = createMakeViewBodyRenderable(view, rootDirective);

        Class<? extends ViewFactory> viewFactoryType = view.getViewFactoryRenderable();

        return factoryDelegate.make(viewFactoryType, body);
    }

    private MakeViewBody createMakeViewBodyRenderable(ViewNode view, Directive rootDirective) {
        if (view.isUsingIsolateDirective()) {
            return new DirectiveMakeViewBody(view, rootDirective);
        } else {
            return new MakeViewBody(view, rootDirective);
        }
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
}
