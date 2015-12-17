package com.flarestar.drones.mvw.processing.writer;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.processing.StyleProcessor;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jtwig.JtwigModelMap;
import org.jtwig.configuration.JtwigConfiguration;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * TODO
 */
public class Generator extends com.flarestar.drones.base.generation.Generator {
    private final StyleProcessor styleProcessor;
    private final Interpolator interpolator;
    private final TypeInferer typeInferer;
    private final ScopePropertyValueDeducer scopePropertyValueDeducer;

    @Inject
    public Generator(Provider<JtwigConfiguration> jtwigConfigProvider, ProcessingEnvironment processingEnvironment,
                     StyleProcessor styleProcessor, Interpolator interpolator, TypeInferer typeInferer,
                     ScopePropertyValueDeducer scopePropertyValueDeducer) {
        super(jtwigConfigProvider, processingEnvironment);

        this.styleProcessor = styleProcessor;
        this.interpolator = interpolator;
        this.typeInferer = typeInferer;
        this.scopePropertyValueDeducer = scopePropertyValueDeducer;
    }

    @Override
    protected JtwigModelMap getModel(Renderable renderable) {
        JtwigModelMap model = super.getModel(renderable);
        model.add("styleProcessor", styleProcessor);
        model.add("interpolator", interpolator);
        model.add("typeInferer", typeInferer);
        model.add("scopePropertyValueDeducer", scopePropertyValueDeducer);
        return model;
    }
}
