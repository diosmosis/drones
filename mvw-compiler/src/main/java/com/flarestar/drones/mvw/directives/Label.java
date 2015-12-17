package com.flarestar.drones.mvw.directives;

import android.widget.TextView;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.mvw.annotations.directive.DirectiveName;
import com.flarestar.drones.mvw.annotations.directive.DirectiveView;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.processing.parser.directive.matchers.TagMatcher;

@DirectiveName("label")
@DirectiveMatcher(TagMatcher.class)
@DirectiveView(view = TextView.class)
public class Label extends Directive {
    public Label(GenerationContext context) throws LayoutFileException {
        super(context);
    }
}
