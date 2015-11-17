package com.flarestar.drones.layout.view.directive;

import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;

/**
 * TODO
 */
public interface DirectiveMatcher {

    boolean matches(ViewNode node, Directive directive);
}
