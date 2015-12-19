package com.flarestar.drones.mvw;

import com.asual.lesscss.LessEngine;
import com.flarestar.drones.mvw.directives.Repeat;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.NullViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.RangeViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.SingleViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.processing.writer.Generator;
import com.helger.css.ECSSVersion;
import com.helger.css.writer.CSSWriterSettings;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

public class DroneModule extends com.flarestar.drones.base.DroneModule {

    private RoundEnvironment roundEnvironment;

    public DroneModule(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
        super(processingEnvironment);

        this.roundEnvironment = roundEnvironment;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(RoundEnvironment.class).toInstance(roundEnvironment);
        bind(CSSWriterSettings.class).toInstance(new CSSWriterSettings(ECSSVersion.CSS30));
        bind(LessEngine.class).toInstance(new LessEngine());
        bind(com.flarestar.drones.base.generation.Generator.class).to(Generator.class);

        // TODO: there's currently no way to register user-defined types this way. not sure how to make it possible...
        //       user defined modules? how would I get them here?
        bindInstanceFactory(ViewFactory.InstanceFactory.class, ViewFactory.class, SingleViewFactory.class);
        bindInstanceFactory(ViewFactory.InstanceFactory.class, ViewFactory.class, NullViewFactory.class);
        bindInstanceFactory(ViewFactory.InstanceFactory.class, ViewFactory.class, Repeat.RepeatViewFactoryRenderable.class);
    }
}
