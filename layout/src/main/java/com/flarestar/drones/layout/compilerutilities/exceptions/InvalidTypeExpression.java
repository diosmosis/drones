package com.flarestar.drones.layout.compilerutilities.exceptions;

public class InvalidTypeExpression extends BaseExpressionException {
    public InvalidTypeExpression(String expression, String reason) {
        super(createMessage(expression, reason));
    }

    public InvalidTypeExpression(String expression, String reason, Throwable cause) {
        super(createMessage(expression, reason), cause);
    }

    public InvalidTypeExpression(String expression, String reason, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(createMessage(expression, reason), cause, enableSuppression, writableStackTrace);
    }

    private static String createMessage(String expression, String reason) {
        return "Invalid expression '" + expression + "': " + reason;
    }
}
