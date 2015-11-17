package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;

/**
 * TODO
 */
@DirectiveMatcher(AttributeMatcher.class)
public class Click extends Directive {

    @Override
    public void onViewCreated(ViewNode node, StringBuilder result) throws LayoutFileException {
        String code = node.attributes.get("ng-click");

        {
            result.append("realScreen.");
            result.append(node.id);
            result.append(".setOnClickListener(new View.OnClickListener() {\n");
        }

        result.append("    public void onClick(View v) {\n");
        result.append("        try {\n");

        {
            String scopeClassName = node.getScopeDefinition().getScopeClassName();

            result.append("            ");
            result.append(scopeClassName);
            result.append(" scope = ");
            result.append(node.getScopeVarName());
            result.append(";\n");
        }

        {
            result.append("            ");
            result.append(code);
            result.append(";\n");
        }

        result.append("        } catch (Exception e) {\n");
        result.append("            throw new RuntimeException(\"Unexpected error when invoking event handler.\", e);\n");
        result.append("        }\n");
        result.append("    }\n");
        result.append("});\n");
    }

    @Override
    public String getDirectiveName() {
        return "ng-click";
    }
}
