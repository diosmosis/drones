package com.flarestar.drones.layout.view.directive.matchers;

import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.DirectiveMatcher;
import org.jsoup.nodes.Element;

/**
 * TODO
 */
public class AttributeMatcher implements DirectiveMatcher {

    @Override
    public boolean matches(Element node, Class<?> directiveClass) {
        return node.attributes().hasKey(Directive.getDirectiveName(directiveClass));
    }
}
