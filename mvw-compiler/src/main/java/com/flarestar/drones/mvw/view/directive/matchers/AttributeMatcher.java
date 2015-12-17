package com.flarestar.drones.mvw.view.directive.matchers;

import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.directive.DirectiveMatcher;
import org.jsoup.nodes.Element;

/**
 * TODO
 */
public class AttributeMatcher implements DirectiveMatcher {

    @Override
    public boolean matches(Element node, Class<?> directiveClass) {
        return node.hasAttr(Directive.getDirectiveName(directiveClass));
    }
}
