package com.flarestar.drones.mvw.processing.writer;

import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.flarestar.drones.mvw.processing.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.LayoutBuilder;
import com.flarestar.drones.mvw.view.ViewNode;
import com.google.inject.Inject;
import org.jtwig.exception.JtwigException;

import java.io.IOException;

/**
 * TODO
 */
public class LayoutBuilderWriter {

    private final IsolateDirectiveProcessor isolateDirectiveProcessor;
    private final FunctionSniffer functionSniffer;
    private final Generator generator;

    @Inject
    public LayoutBuilderWriter(IsolateDirectiveProcessor isolateDirectiveProcessor, FunctionSniffer functionSniffer,
                               Generator generator) {
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;
        this.functionSniffer = functionSniffer;
        this.generator = generator;
    }

    public void writeLayoutBuilder(ActivityGenerationContext context, ViewNode tree)
            throws LayoutFileException, InvalidUserFunctionClass, IOException, JtwigException {
        LayoutBuilder builder = new LayoutBuilder(context, tree, functionSniffer, isolateDirectiveProcessor);
        generator.renderClass(builder);
    }
}
