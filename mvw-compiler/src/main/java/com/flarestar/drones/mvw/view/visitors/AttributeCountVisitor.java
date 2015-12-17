package com.flarestar.drones.mvw.view.visitors;

import com.flarestar.drones.mvw.view.ViewNode;

/**
 * TODO
 */
public class AttributeCountVisitor implements ViewNode.Visitor {

    public int count = 0;

    @Override
    public void visit(ViewNode node) {
        if (node.element.hasAttr("ng-controller")) {
            ++count;
        }
    }
}
