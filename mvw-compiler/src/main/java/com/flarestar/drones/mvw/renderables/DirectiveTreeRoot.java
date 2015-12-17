package com.flarestar.drones.mvw.renderables;

import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.helger.commons.microdom.util.MicroVisitor;

/**
 * TODO
 */
public class DirectiveTreeRoot {
    private DirectiveScopeInterface scopeInterface;
    private MakeViewMethod directiveMakeView;
    private ViewNode viewNode;

    public DirectiveTreeRoot(Directive directive, ViewNode tree) throws LayoutFileException {
        this.viewNode = tree;
        this.scopeInterface = new DirectiveScopeInterface(directive);
        this.directiveMakeView = new MakeViewMethod(tree, directive);
    }

    public DirectiveScopeInterface getScopeInterface() {
        return scopeInterface;
    }

    public MakeViewMethod getDirectiveMakeView() {
        return directiveMakeView;
    }

    public ViewNode getViewNode() {
        return viewNode;
    }
}
