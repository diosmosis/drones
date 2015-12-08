package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.flarestar.drones.views.ViewFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 */
public abstract class DynamicViewGroup extends BaseDroneViewGroup {

    protected class ChildViewCreatorIterator {
        private Iterator<ViewFactory> childDefinitionIterator = DynamicViewGroup.this.childDefinitions.iterator();
        private ViewFactory.Iterator current = null;

        public ChildViewCreatorIterator() {
            if (childDefinitionIterator.hasNext()) {
                current = childDefinitionIterator.next().iterator();
            }
        }

        public boolean hasNext() {
            return (this.current != null && current.hasNext()) || childDefinitionIterator.hasNext();
        }

        public void next() {
            if (current == null || !current.hasNext()) {
                while (true) {
                    if (!childDefinitionIterator.hasNext()) {
                        current = null;
                        break;
                    }

                    current = childDefinitionIterator.next().iterator();

                    if (current.hasNext()) {
                        break;
                    }
                }
            } else {
                current.next();
            }
        }

        public View makeView() {
            return current.makeView();
        }
    }

    /**
     * TODO
     */
    protected List<ViewFactory> childDefinitions = new ArrayList<>();

    public DynamicViewGroup(Context context) {
        super(context);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // TODO: automated tests should detect if memory leaks exist
    // TODO: only add Views if they are in the viewport. HOW TO DO THISSSS??? let's do it in boxmodelnode.

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    protected ChildViewCreatorIterator viewCreationIterator() {
        return new ChildViewCreatorIterator();
    }
}
