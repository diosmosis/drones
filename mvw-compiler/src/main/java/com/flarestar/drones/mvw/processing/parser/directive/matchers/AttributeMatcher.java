package com.flarestar.drones.mvw.processing.parser.directive.matchers;

import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.processing.parser.directive.DirectiveMatcher;
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
