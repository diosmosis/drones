package com.flarestar.drones.mvw;

import com.flarestar.drones.base.BaseScreen;
import com.flarestar.drones.mvw.android.exceptions.InvalidManifestException;
import com.flarestar.drones.mvw.annotations.Layout;
import com.flarestar.drones.mvw.compilerutilities.ProjectSniffer;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.context.ActivityGenerationContext;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.parser.IsolateDirectiveProcessor;
import com.flarestar.drones.mvw.parser.LayoutProcessor;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.writer.LayoutBuilderWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.*;
import java.io.*;

/**
 * TODO
 *
 * TODO: text nodes in viewgroup nodes should result in an error
 */
@Singleton
public class LayoutBuilderGenerator {

    private LayoutProcessor xmlProcessor;
    private LayoutBuilderWriter layoutBuilderWriter;
    private ProcessingEnvironment processingEnvironment;
    private TypeMirror screenType;
    private ProjectSniffer projectSniffer;
    private TypeInferer typeInferer;
    private IsolateDirectiveProcessor isolateDirectiveProcessor;

    @Inject
    public LayoutBuilderGenerator(LayoutProcessor xmlProcessor, LayoutBuilderWriter layoutBuilderWriter,
                                  ProcessingEnvironment processingEnvironment, ProjectSniffer projectSniffer,
                                  TypeInferer typeInferer, IsolateDirectiveProcessor isolateDirectiveProcessor) {
        this.xmlProcessor = xmlProcessor;
        this.layoutBuilderWriter = layoutBuilderWriter;
        this.processingEnvironment = processingEnvironment;
        this.projectSniffer = projectSniffer;
        this.typeInferer = typeInferer;
        this.isolateDirectiveProcessor = isolateDirectiveProcessor;

        try {
            this.screenType = typeInferer.getTypeMirrorFor(BaseScreen.class.getName());
        } catch (IllegalStateException ex) {
            throw new RuntimeException("Cannot find the '" + BaseScreen.class.getName() + "' type, is the base drone on the classpath?", ex);
        }
    }

    public boolean canGenerateFor(Element element) {
        if (!(element instanceof TypeElement)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unexpected state, element '"
                + element.getSimpleName() + "' is not a TypeElement. Skipping.");

            return false;
        }

        if (element.getKind() != ElementKind.CLASS) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Element '" + element.getSimpleName() + "' is not a class, only class elements should be "
                    + "annotated w/ @Layout");

            return false;
        }

        if (!processingEnvironment.getTypeUtils().isAssignable(element.asType(), screenType)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Element '" + element.getSimpleName() + "' must derive from BaseScreen in order to be used "
                    + "w/ @Layout.");

            return false;
        }

        return true;
    }

    public void generateLayoutBuilderFor(TypeElement activityClassElement) {
        final ActivityGenerationContext context;
        try {
            context = new ActivityGenerationContext(activityClassElement, projectSniffer);
        } catch (InvalidManifestException e) {
            throw new RuntimeException(e);
        }

        typeInferer.setBasePackage(context.getActivityPackage()); // TODO: should not have this mutability, but not sure how to remove...

        ViewNode tree = processLayoutAndStyles(activityClassElement, context);
        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasIsolateDirective()) {
                    try {
                        isolateDirectiveProcessor.processIsolateDirectives(context, node);
                    } catch (LayoutFileException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        generateLayoutBuilder(context, tree);
    }

    private ViewNode processLayoutAndStyles(TypeElement activityClassElement, GenerationContext context) {
        Layout annotation = activityClassElement.getAnnotation(Layout.class);
        String layoutFilePath = annotation.value();
        String stylesheetFilePath = annotation.stylesheet();

        try {
            return xmlProcessor.processTemplateAndLess(context, layoutFilePath, stylesheetFilePath);
        } catch (LayoutFileException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateLayoutBuilder(final ActivityGenerationContext context, ViewNode tree) {
        String layoutBuilderClassName = context.getLayoutBuilderClassName();
        String screenClassName = context.getActivityClassName();

        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating '" + layoutBuilderClassName + "'.");

        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                for (Directive directive : node.directives) {
                    try {
                        directive.beforeGeneration(node);
                    } catch (LayoutFileException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        JavaFileObject newObject;
        try {
            newObject = processingEnvironment.getFiler().createSourceFile(layoutBuilderClassName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '" + screenClassName + "'.", e);
        }

        try (OutputStream output = newObject.openOutputStream()) {
            layoutBuilderWriter.writeLayoutBuilder(context, tree, output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LayoutBuilder for '"+ screenClassName + "'.", e);
        }
    }
}
