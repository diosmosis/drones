package com.flarestar.drones.mvw.processing.parser;

import com.flarestar.drones.mvw.annotations.directive.IsolateDirective;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

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

    public ViewNode getDirectiveTree(ActivityGenerationContext context, Class<? extends Directive> directiveClass)
            throws LayoutFileException {
        ViewNode tree = directiveTrees.get(directiveClass);
        if (tree == null) {
            tree = processIsolateDirective(context, directiveClass);
            directiveTrees.put(directiveClass, tree);
        }
        return tree;
    }

    // TODO: should use separate generation context for directives (they should be stored in a different layout builder rather than replicated for each activity)
    private ViewNode processIsolateDirective(ActivityGenerationContext context, Class<? extends Directive> directiveClass)
            throws LayoutFileException {
        IsolateDirective annotation = directiveClass.getAnnotation(IsolateDirective.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Invalid directive supplied for 'directiveClass': "
                + directiveClass.getName() + " is not an isolate directive.");
        }

        return processor.processTemplateAndLess(context, annotation.template(), annotation.less(), directiveClass);
    }

    public void processIsolateDirectives(ActivityGenerationContext context, ViewNode viewNode)
            throws LayoutFileException {
        for (Directive directive : viewNode.directives) {
            if (directive.isIsolateDirective()) {
                getDirectiveTree(context, directive.getClass());
            }
        }
    }
}
