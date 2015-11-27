package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.GenerationContext;
import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

@DirectiveName("container")
@DirectiveView(view = com.flarestar.drones.views.viewgroups.Container.class)
@DirectiveMatcher(TagMatcher.class)
public class Container extends Directive {
    public Container(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
