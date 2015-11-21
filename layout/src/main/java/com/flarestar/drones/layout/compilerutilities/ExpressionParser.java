package com.flarestar.drones.layout.compilerutilities;

import com.flarestar.drones.layout.compilerutilities.exceptions.*;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private TypeMirror originalType;
    private String originalExpression;
    private TypeInferer inferer;
    private ScopeDefinition currentScope;

    private String currentExpression;
    private TypeMirror currentResultType;

    public ExpressionParser(TypeMirror type, String expression, ScopeDefinition currentScope, TypeInferer inferer) {
        originalType = type;
        originalExpression = expression;
        this.currentScope = currentScope;
        this.inferer = inferer;
    }

    public TypeMirror getResultType()
            throws InvalidExpression, InvalidTypeException, InvalidTypeExpression, CannotFindProperty, CannotFindMethod {
        currentExpression = originalExpression;
        currentResultType = originalType;

        while (!isFinishedParsingExpression()) {
            parseExpressionChunk();
        }

        return currentResultType;
    }

    private void parseExpressionChunk()
            throws InvalidExpression, InvalidTypeException, InvalidTypeExpression, CannotFindProperty, CannotFindMethod {
        char firstChar = currentExpression.charAt(0);
        switch (firstChar) {
            case '.':
                parseMemberAccess();
                break;
            case '[':
                parseArrayAccess();
                break;
            default:
                throw new InvalidExpression(currentExpression, "don't know how to process expression, stuck at '"
                    + firstChar + "'");
        }
    }

    private void parseMemberAccess()
            throws InvalidExpression, InvalidTypeExpression, CannotFindProperty, CannotFindMethod, InvalidTypeException {
        String[] parts = currentExpression.substring(1).split("[^a-zA-Z0-9_$]", 2);

        String identifier = parts[0];
        currentExpression = parts.length == 2 ? parts[1] : "";

        if (currentExpression.length() > 0 && currentExpression.charAt(0) == '(') {
            currentExpression = currentExpression.substring(1);

            List<TypeMirror> parameters = parseParameterList();

            currentResultType = inferer.getMethodReturnTypeOf(currentResultType, identifier, parameters);
        } else {
            currentResultType = inferer.getPropertyTypeOf(currentResultType, identifier);
        }
    }

    private List<TypeMirror> parseParameterList()
            throws InvalidExpression, InvalidTypeExpression, InvalidTypeException, CannotFindProperty, CannotFindMethod {
        List<TypeMirror> parameters = new ArrayList<>();

        int callScope = 0, currentParameterStart = 0;

        mainparseloop:
        for (int i = 0; true; ++i) {
            if (i >= currentExpression.length()) {
                throw new InvalidExpression(originalExpression, "unexpected end of input (parameter list not terminated)");
            }

            switch (currentExpression.charAt(i)) {
                case ')':
                    if (callScope == 0) {
                        String parameter = currentExpression.substring(currentParameterStart + 1, i);
                        TypeMirror parameterType = currentScope.getTypeOfExpression(parameter);
                        parameters.add(parameterType);

                        break mainparseloop;
                    }

                    --callScope;
                    break;
                case ',':
                    String parameter = currentExpression.substring(currentParameterStart + 1, i);
                    TypeMirror parameterType = currentScope.getTypeOfExpression(parameter);
                    parameters.add(parameterType);

                    currentParameterStart = i;
                    break;
                case '(':
                    ++callScope;
                    break;
                default:
                    break;
            }
        }

        return parameters;
    }

    private void parseArrayAccess() throws InvalidTypeException {
        currentResultType = inferer.getValueTypeOf(currentResultType);
        currentExpression = currentExpression.substring(currentExpression.indexOf(']') + 1);
    }

    private boolean isFinishedParsingExpression() {
        return currentExpression.isEmpty();
    }
}
