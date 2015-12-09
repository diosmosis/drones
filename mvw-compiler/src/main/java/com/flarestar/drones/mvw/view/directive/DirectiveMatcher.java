package com.flarestar.drones.mvw.view.directive;

import org.jsoup.nodes.Element;

/**
 * TODO
 */
public interface DirectiveMatcher {

    boolean matches(Element node, Class<?> directiveClass);
}
