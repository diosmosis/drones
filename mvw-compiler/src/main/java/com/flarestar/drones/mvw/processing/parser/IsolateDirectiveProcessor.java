package com.flarestar.drones.mvw.processing.parser;

import com.flarestar.drones.mvw.annotations.directive.IsolateDirective;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.DirectiveTreeRoot;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;

/**
 * TODO
 */
@Singleton
public class IsolateDirectiveProcessor {

    private LayoutProcessor processor;
    private Map<Class<? extends Directive>, ViewNode> directiveTrees = new HashMap<>();

    @Inject
    public IsolateDirectiveProcessor(LayoutProcessor processor) {
        this.processor = processor;
    }

    public ViewNode getDirectiveTree(Class<? extends Directive> directiveClass) {
        ViewNode tree = directiveTrees.get(directiveClass);
        if (tree == null) {
            throw new IllegalStateException("Directive '" + directiveClass.getName() + "' has not been processed yet.");
        }
        return tree;
    }

    // TODO: should use separate generation context for directives (they should be stored in a different layout builder rather than replicated for each activity)
    private void processIsolateDirective(ActivityGenerationContext context, Class<? extends Directive> directiveClass)
            throws LayoutFileException {
        IsolateDirective annotation = directiveClass.getAnnotation(IsolateDirective.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Invalid directive supplied for 'directiveClass': "
                + directiveClass.getName() + " is not an isolate directive.");
        }

        ViewNode tree = processor.processTemplateAndLess(context, annotation.template(), annotation.less(), directiveClass);
        directiveTrees.put(directiveClass, tree);
    }

    public void processIsolateDirectives(ActivityGenerationContext context, ViewNode viewNode)
            throws LayoutFileException {
        for (Directive directive : viewNode.directives) {
            if (!directive.isIsolateDirective()
                || directiveTrees.containsKey(directive.getClass())
            ) {
                continue;
            }

            processIsolateDirective(context, directive.getClass());
        }
    }
}
