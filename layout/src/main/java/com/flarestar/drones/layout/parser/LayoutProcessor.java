package com.flarestar.drones.layout.parser;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.flarestar.drones.layout.parser.exceptions.InvalidLayoutAttributeValue;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.parser.exceptions.UnknownLayoutAttributeException;
import com.flarestar.drones.layout.view.ViewNode;
import com.flarestar.drones.layout.view.directive.DirectiveFactory;
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
public class LayoutProcessor {

    private CSSWriterSettings cssWriterSettings;
    private DirectiveFactory directiveFactory;

    public LayoutProcessor() {
        this.cssWriterSettings = new CSSWriterSettings(ECSSVersion.CSS30);
        this.directiveFactory = new DirectiveFactory();
    }

    public ViewNode createViewTree(InputStream layoutInput, InputStream styleSheetInput) throws LayoutFileException {
        // handle layout file
        Document document = parseXmlDocument(layoutInput);

        Map<Element, ViewNode> allNodes = new HashMap<>();
        ViewNode tree = makeViewNode(document.children().first(), allNodes, null);

        // handle stylesheet file
        if (styleSheetInput !=  null) {
            CascadingStyleSheet styleSheet = parseStyleSheet(styleSheetInput);
            processStyleSheet(styleSheet, document, allNodes);
        }

        return tree;
    }

    private void processStyleSheet(CascadingStyleSheet styleSheet, Document document, Map<Element, ViewNode> allNodes) {
        for (CSSStyleRule rule : styleSheet.getAllStyleRules()) {
            for (CSSSelector selector : rule.getAllSelectors()) {
                String selectorString = selector.getAsCSSString(cssWriterSettings, 0);

                Elements elements = document.select(selectorString);
                for (Element element : elements) {
                    ViewNode node = allNodes.get(element);
                    if (node == null) {
                        continue;
                    }

                    addStylesToNode(node, rule);
                }
            }
        }
    }

    private void addStylesToNode(ViewNode node, CSSStyleRule rule) {
        for (CSSDeclaration decl : rule.getAllDeclarations()) {
            String property = decl.getProperty();
            String expression = decl.getExpression().getAsCSSString(cssWriterSettings, 0);

            node.styles.put(property, expression);
        }
    }

    private CascadingStyleSheet parseStyleSheet(InputStream styleSheetInput) throws LayoutFileException {
        // parse LESS
        LessEngine engine = new LessEngine();

        String cssContent;
        try {
            cssContent = engine.compile(readWholeInputStream(styleSheetInput));
        } catch (LessException e) {
            throw new LayoutFileException("Bad LESS stylesheet", e);
        }

        return CSSReader.readFromString(cssContent, CCharset.CHARSET_UTF_8_OBJ, ECSSVersion.CSS30);
    }

    private String readWholeInputStream(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ViewNode makeViewNode(Element node, Map<Element, ViewNode> allNodes, ViewNode parent)
            throws LayoutFileException {
        String tagName = node.tagName();

        String id = node.attr("id");
        if (id == null) {
            throw new LayoutFileException("Node '" + tagName + "' is missing the id parameter.");
        }

        String text = node.ownText();

        ViewNode viewNode = new ViewNode(tagName, id, text, parent);
        processNodeAttributes(node, viewNode);
        directiveFactory.detectDirectives(viewNode);
        allNodes.put(node, viewNode);

        processNodeChildren(node, viewNode, allNodes);

        return viewNode;
    }

    private void processNodeAttributes(Element node, ViewNode viewNode)
            throws UnknownLayoutAttributeException, InvalidLayoutAttributeValue {
        for (Attribute attribute : node.attributes()) {
            String attributeName = attribute.getKey();
            String attributeValue = attribute.getValue();
            viewNode.attributes.put(attributeName, attributeValue);
        }
    }

    private void processNodeChildren(Element node, ViewNode viewNode, Map<Element, ViewNode> allNodes)
            throws LayoutFileException {
        for (Element child : node.children()) {
            ViewNode childViewNode = makeViewNode(child, allNodes, viewNode);
            viewNode.children.add(childViewNode);
        }
    }

    private Document parseXmlDocument(InputStream input) {
        try {
            return Jsoup.parse(input, "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            throw new RuntimeException("Unabe to read input.", e);
        }
    }
}
