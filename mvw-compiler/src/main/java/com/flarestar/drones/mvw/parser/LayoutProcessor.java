package com.flarestar.drones.mvw.parser;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.flarestar.drones.mvw.GenerationContext;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.directive.DirectiveFactory;
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
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

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
    private int lastGeneratedId = 0;

    @Inject
    public LayoutProcessor(DirectiveFactory directiveFactory, CSSWriterSettings cssWriterSettings, LessEngine lessEngine) {
        this.cssWriterSettings = cssWriterSettings;
        this.directiveFactory = directiveFactory;
        this.lessEngine = lessEngine;
    }

    public ViewNode createViewTree(GenerationContext context, InputStream layoutInput, InputStream styleSheetInput)
            throws LayoutFileException {
        // handle layout file
        Document document = parseXmlDocument(layoutInput);

        Table<Element, String, String> stylesByElement = HashBasedTable.create();

        CascadingStyleSheet styleSheet = null;
        if (styleSheetInput !=  null) {
            styleSheet = parseStyleSheet(styleSheetInput);
            processStyleSheet(styleSheet, document, stylesByElement);
        }

        return makeViewNode(document.children().first(), null, context, stylesByElement);
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
                                  Table<Element, String, String> stylesByElement)
            throws LayoutFileException {
        String tagName = node.tagName();

        String id = node.attr("id");
        if (id == null || id.isEmpty()) {
            id = generateViewId();
        }

        String text = node.ownText();

        ViewNode viewNode = new ViewNode(tagName, id, text, parent, attributeMap(node), stylesByElement.row(node),
            directiveFactory.detectDirectives(node, context));
        processNodeChildren(node, viewNode, context, stylesByElement);
        return viewNode;
    }

    private Map<String, String> attributeMap(Element node) {
        Map<String, String> result = new HashMap<>();
        for (Attribute attribute : node.attributes()) {
            result.put(attribute.getKey(), attribute.getValue());
        }
        return result;
    }

    private String generateViewId() {
        int nextId = ++lastGeneratedId;
        return "view" + nextId;
    }

    private void processNodeChildren(Element node, ViewNode viewNode, GenerationContext context,
                                     Table<Element, String, String> stylesByElement)
            throws LayoutFileException {
        for (Element child : node.children()) {
            ViewNode childViewNode = makeViewNode(child, viewNode, context, stylesByElement);
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
}
