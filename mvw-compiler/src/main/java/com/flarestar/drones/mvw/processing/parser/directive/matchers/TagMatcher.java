package com.flarestar.drones.mvw.processing.parser.directive.matchers;

    import com.flarestar.drones.mvw.model.Directive;
    import com.flarestar.drones.mvw.processing.parser.directive.DirectiveMatcher;
    import org.jsoup.nodes.Element;

/**
 * TODO
 */
public class TagMatcher implements DirectiveMatcher {

    @Override
    public boolean matches(Element node, Class<?> directiveClass) {
        String directiveName = Directive.getDirectiveName(directiveClass);
        return node.tagName().equals(directiveName);
    }
}
