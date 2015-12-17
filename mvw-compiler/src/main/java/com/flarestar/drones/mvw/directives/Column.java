package com.flarestar.drones.mvw.directives;

import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.TagMatcher;

@DirectiveName("column")
@DirectiveView(view = com.flarestar.drones.views.viewgroups.Column.class)
@DirectiveMatcher(TagMatcher.class)
public class Column extends Directive {
    public Column(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
