package com.flarestar.drones.mvw.writer;

import com.flarestar.drones.mvw.function.FunctionDefinition;
import com.flarestar.drones.mvw.function.FunctionSniffer;
import com.flarestar.drones.mvw.function.exceptions.InvalidUserFunctionClass;
import com.flarestar.drones.mvw.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.renderables.DirectiveTreeRoot;
import com.flarestar.drones.mvw.renderables.LayoutBuilder;
import com.flarestar.drones.mvw.renderables.scope.ScopeDefinition;
import com.flarestar.drones.mvw.renderables.TemplateFunctionProxyCode;
import com.flarestar.drones.mvw.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.StyleProcessor;
import com.flarestar.drones.mvw.view.ViewNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.jtwig.JtwigModelMap;
import org.jtwig.JtwigTemplate;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.exception.JtwigException;
import org.jtwig.loader.Loader;
import org.jtwig.loader.impl.ClasspathLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

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
