package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.mvw.renderables.scope.ScopeEventListener;

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
        events.add(new ScopeEventListener(com.flarestar.drones.views.scope.events.Click.class, node.element.attr("ng-click")));
    }

    @Override
    public String getDirectiveName() {
        return "ng-click";
    }
}
