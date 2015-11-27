package com.flarestar.drones.layout.view.directive;

import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import org.jsoup.nodes.Element;

/**
 * TODO
 */
public interface DirectiveMatcher {

    boolean matches(Element node, Class<?> directiveClass);
}
