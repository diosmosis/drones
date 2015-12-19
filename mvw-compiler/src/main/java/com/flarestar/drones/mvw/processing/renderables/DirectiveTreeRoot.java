package com.flarestar.drones.mvw.processing.renderables;

import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewMethod;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;

/**
 * TODO
 */
public class DirectiveTreeRoot {
    private DirectiveScopeInterface scopeInterface;
    private MakeViewMethod directiveMakeView;
    private ViewNode viewNode;

    public DirectiveTreeRoot(Directive directive, ViewNode tree, MakeViewMethod directiveMakeView) {
        this.viewNode = tree;
        this.scopeInterface = new DirectiveScopeInterface(directive);
        this.directiveMakeView = directiveMakeView;
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
