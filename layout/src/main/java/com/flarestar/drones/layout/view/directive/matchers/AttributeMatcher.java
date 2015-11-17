package com.flarestar.drones.layout.view.directive.matchers;

import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.DirectiveMatcher;

/**
 * TODO
 */
public class AttributeMatcher implements DirectiveMatcher {

    @Override
    public boolean matches(ViewNode node, Directive directive) {
        return node.attributes.containsKey(directive.getDirectiveName());
    }
}
