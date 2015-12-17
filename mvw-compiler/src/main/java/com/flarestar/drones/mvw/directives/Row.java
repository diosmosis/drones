package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.directive.matchers.TagMatcher;

@DirectiveName("row")
@DirectiveView(view = com.flarestar.drones.views.viewgroups.Row.class)
@DirectiveMatcher(TagMatcher.class)
public class Row extends Directive {
    public Row(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
