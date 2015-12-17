package com.flarestar.drones.mvw.processing.parser;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.flarestar.drones.mvw.context.GenerationContext;
import com.flarestar.drones.mvw.processing.parser.exceptions.ControllerInDirectiveTemplateNotAllowed;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.MultipleElementsWithTransclude;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.StyleProcessor;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.DirectiveFactory;
import com.flarestar.drones.mvw.view.visitors.AttributeCountVisitor;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.helger.commons.charset.CCharset;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSSelector;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriterSettings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * TODO
 */
@Singleton
public class LayoutProcessor {

    private CSSWriterSettings cssWriterSettings;
    private DirectiveFactory directiveFactory;
    private LessEngine lessEngine;
    private ProcessingEnvironment processingEnvironment;
    private StyleProcessor styleProcessor;
    private int lastGeneratedId = 0;

    @Inject
    public LayoutProcessor(DirectiveFactory directiveFactory, CSSWriterSettings cssWriterSettings, LessEngine lessEngine,
                           ProcessingEnvironment processingEnvironment, StyleProcessor styleProcessor) {
        this.cssWriterSettings = cssWriterSettings;
        this.directiveFactory = directiveFactory;
        this.lessEngine = lessEngine;
        this.processingEnvironment = processingEnvironment;
        this.styleProcessor = styleProcessor;
    }

    private ViewNode createViewTree(GenerationContext context, InputStream layoutInput, InputStream styleSheetInput,
                                    Class<?> rootDirective) throws LayoutFileException {
        Document document = parseXmlDocument(layoutInput);

        Table<Element, String, String> stylesByElement = HashBasedTable.create();

        if (styleSheetInput != null) {
            CascadingStyleSheet styleSheet = parseStyleSheet(styleSheetInput);
            processStyleSheet(styleSheet, document, stylesByElement);
        }

        return makeViewNode(document.children().first(), null, context, stylesByElement, rootDirective);
    }

    private void processStyleSheet(CascadingStyleSheet styleSheet, Document document,
                                   Table<Element, String, String> stylesByElement) {
        for (CSSStyleRule rule : styleSheet.getAllStyleRules()) {
            for (CSSSelector selector : rule.getAllSelectors()) {
                String selectorString = selector.getAsCSSString(cssWriterSettings, 0);

                Elements elements = document.select(selectorString);
                for (Element element : elements) {
                    addStylesToNode(element, rule, stylesByElement);
                }
            }
        }
    }

    private void addStylesToNode(Element element, CSSStyleRule rule, Table<Element, String, String> stylesByElement) {
        for (CSSDeclaration decl : rule.getAllDeclarations()) {
            String property = decl.getProperty();
            String expression = decl.getExpression().getAsCSSString(cssWriterSettings, 0);

            stylesByElement.put(element, property, expression);
        }
    }

    private CascadingStyleSheet parseStyleSheet(InputStream styleSheetInput) throws LayoutFileException {
        String cssContent;
        try {
            cssContent = lessEngine.compile(readWholeInputStream(styleSheetInput));
        } catch (LessException e) {
            throw new LayoutFileException("Bad LESS stylesheet", e);
        }

        return CSSReader.readFromString(cssContent, CCharset.CHARSET_UTF_8_OBJ, ECSSVersion.CSS30);
    }

    private String readWholeInputStream(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ViewNode makeViewNode(Element node, ViewNode parent, GenerationContext context,
                                  Table<Element, String, String> stylesByElement, Class<?> directiveClassToApply)
            throws LayoutFileException {
        boolean isDirectiveRoot = false;

        List<Directive> directives = directiveFactory.detectDirectives(node, context);
        if (directiveClassToApply != null) {
            Directive directive = directiveFactory.makeRootDirective(context, directiveClassToApply);
            directives.add(directive);

            isDirectiveRoot = true;
        }

        ViewNode viewNode = new ViewNode(node, parent, stylesByElement.row(node), directives, isDirectiveRoot);
        styleProcessor.process(viewNode);

        processNodeChildren(node, viewNode, context, stylesByElement);
        return viewNode;
    }

    private String generateViewId() {
        int nextId = ++lastGeneratedId;
        return "view" + nextId;
    }

    private void processNodeChildren(Element node, ViewNode viewNode, GenerationContext context,
                                     Table<Element, String, String> stylesByElement)
            throws LayoutFileException {
        for (Element child : node.children()) {
            ViewNode childViewNode = makeViewNode(child, viewNode, context, stylesByElement, null);
            viewNode.children.add(childViewNode);
        }
    }

    private Document parseXmlDocument(InputStream input) {
        try {
            return Jsoup.parse(input, "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read input.", e);
        }
    }

    public ViewNode processTemplateAndLess(GenerationContext context, String template, String less)
            throws LayoutFileException {
        return processTemplateAndLess(context, template, less, null);
    }

    public ViewNode processTemplateAndLess(GenerationContext context, String template, String less,
                                           Class<?> rootDirective) throws LayoutFileException {
        FileObject templateFileObject = getResource(template);

        FileObject stylesheetFileObject = null;
        if (!less.isEmpty()) {
            stylesheetFileObject = getResource(less);
        }

        ViewNode result;
        try (InputStream layoutInput = templateFileObject.openInputStream();
             InputStream stylesheetInput = stylesheetFileObject == null ? null : stylesheetFileObject.openInputStream()
        ) {
            result = createViewTree(context, layoutInput, stylesheetInput, rootDirective);
        } catch (LayoutFileException e) {
            throw new RuntimeException("Layout file " + template + " is malformed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + template + " layout file.", e);
        }

        if (rootDirective != null) {
            if (Directive.hasTransclude(rootDirective)) {
                checkForSingleTranscludeChild(rootDirective, result);
            }

            checkTreeDoesNotUseNgController(rootDirective, result);
        }

        return result;
    }


    private void checkTreeDoesNotUseNgController(final Class<?> directive, ViewNode tree)
            throws ControllerInDirectiveTemplateNotAllowed {
        AttributeCountVisitor visitor = new AttributeCountVisitor();
        tree.visit(visitor);

        if (visitor.count > 0) {
            throw new ControllerInDirectiveTemplateNotAllowed(directive);
        }
    }

    private void checkForSingleTranscludeChild(Class<?> rootDirective, ViewNode tree)
            throws LayoutFileException {
        final List<ViewNode> nodesWithTransclude = new ArrayList<>();
        tree.visit(new ViewNode.Visitor() {
            @Override
            public void visit(ViewNode node) {
                if (node.hasTransclude()) {
                    nodesWithTransclude.add(node);
                }
            }
        });

        if (nodesWithTransclude.size() > 1) {
            throw new MultipleElementsWithTransclude(rootDirective, nodesWithTransclude);
        }
    }

    private FileObject getResource(String path) {
        try {
            return processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", path);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: cannot open " + path, e);
        }
    }
}
