package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveName("container")
@DirectiveView(view = com.flarestar.drones.views.Container.class)
@DirectiveMatcher(TagMatcher.class)
public class Container extends Directive {
    public Container(ViewNode node) {
        super(node);
    }
}
