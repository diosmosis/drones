package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;
import com.flarestar.drones.layout.view.scope.Event;

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
        events.add(new Event(com.flarestar.drones.views.scope.events.Click.class, node.attributes.get("ng-click")));
    }

    @Override
    public String getDirectiveName() {
        return "ng-click";
    }
}
