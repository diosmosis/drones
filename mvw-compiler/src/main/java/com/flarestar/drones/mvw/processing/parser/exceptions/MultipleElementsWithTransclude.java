package com.flarestar.drones.mvw.processing.parser.exceptions;

import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;

import java.util.List;

/**
 * TODO
 */
public class MultipleElementsWithTransclude extends LayoutFileException {
    public MultipleElementsWithTransclude(Class<?> directive, List<ViewNode> nodesWithTransclude) {
        super(createMessage(directive, nodesWithTransclude));
    }

    private static String createMessage(Class<?> directive, List<ViewNode> nodesWithTransclude) {
        StringBuilder result = new StringBuilder();
        result.append("The directive '");
        result.append(Directive.getDirectiveName(directive));
        result.append("' has multiple child elements with transclude: ");

        boolean isFirst = true;
        for (ViewNode node : nodesWithTransclude) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(", ");
            }

            result.append("<");
            result.append(node.element.tagName());
            result.append(" id='");
            result.append(node.id);
            result.append("'>");
        }

        return result.toString();
    }
}
