package com.flarestar.drones.mvw.function;

import com.flarestar.drones.mvw.annotations.Function;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
@Singleton
public class FunctionSniffer {
    private TypeInferer typeInferer;
    private List<FunctionDefinition> userFunctions;

    @Inject
    public FunctionSniffer(TypeInferer typeInferer) throws InvalidUserFunctionClass {
        this.typeInferer = typeInferer;
        this.userFunctions = detectUserFunctions();
    }

    public List<FunctionDefinition> getUserFunctions() {
        return userFunctions;
    }

    private List<FunctionDefinition> detectUserFunctions() throws InvalidUserFunctionClass {
        List<FunctionDefinition> result = new ArrayList<>();

        Set<? extends Element> elements = typeInferer.getAllTypesWithAnnotation(Function.class);
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }

            TypeElement userFunctionElement = (TypeElement)element;
            result.add(new FunctionDefinition(userFunctionElement));
        }
        return result;
    }
}
