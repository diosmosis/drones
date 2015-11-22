package com.flarestar.drones.layout.directives;

import com.flarestar.drones.layout.annotations.directive.DirectiveMatcher;
import com.flarestar.drones.layout.annotations.directive.DirectiveName;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.matchers.AttributeMatcher;

/**
 * TODO
 */
@DirectiveName("ng-click")
@DirectiveMatcher(AttributeMatcher.class)
public class Click extends Directive {

    public Click(ViewNode node) {
        super(node);
    }

    @Override
    public String afterViewCreated() throws LayoutFileException {
        StringBuilder result = new StringBuilder();

        String code = node.attributes.get("ng-click");

        result.append("result.setOnClickListener(new View.OnClickListener() {\n");
        result.append("    public void onClick(View v) {\n");
        result.append("        try {\n");

        {
            result.append("            ");
            result.append(code);
            result.append(";\n");
        }

        result.append("            scope.apply();\n");
        result.append("        } catch (Exception e) {\n");
        result.append("            throw new RuntimeException(\"Unexpected error when invoking event handler.\", e);\n");
        result.append("        }\n");
        result.append("    }\n");
        result.append("});\n");

        return result.toString();
    }

    @Override
    public String getDirectiveName() {
        return "ng-click";
    }
}
