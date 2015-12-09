package com.flarestar.drones.mvw.compilerutilities.exceptions;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class CannotFindMethod extends BaseExpressionException {
    public CannotFindMethod(String methodName, TypeMirror type, List<TypeMirror> parameters) {
        super(createMessage(methodName, type, parameters));
    }

    private static String createMessage(String methodName, TypeMirror type, List<TypeMirror> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cannot find method ");
        builder.append(methodName);
        builder.append("(");

        boolean isFirst = true;
        for (TypeMirror parameter : parameters) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(",");
            }

            builder.append(parameter.toString());
        }

        builder.append(") in type '");
        builder.append(type.toString());
        builder.append("'.");
        return builder.toString();
    }
}
