package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;

/**
 * TODO
 */
@DirectiveName("ng-click")
@DirectiveMatcher(AttributeMatcher.class)
public class Click extends Directive {

    public Click(GenerationContext context) throws LayoutFileException {
        super(context);
    }

    @Override
    public void manipulateViewNode(ViewNode node) throws LayoutFileException {
        ScopeEventListener listener =
            new ScopeEventListener(com.flarestar.drones.views.scope.events.Click.class, node.element.attr("ng-click"));
        node.scopeDefinition.events.add(listener);
    }
}
