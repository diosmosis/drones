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
                current = childDefinitionIterator.next().iterator(DynamicViewGroup.this);
            }
        }

        public boolean atEnd() {
            return (current == null || !current.hasNext()) && !childDefinitionIterator.hasNext();
        }

        public void next() {
            if (current != null) {
                current.next();
            }

            if (current == null || !current.hasNext()) {
                while (true) {
                    if (!childDefinitionIterator.hasNext()) {
                        current = null;
                        break;
                    }

                    current = childDefinitionIterator.next().iterator(DynamicViewGroup.this);

                    if (current.hasNext()) {
                        break;
                    }
                }
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

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void moveView(int from, int to) {
        View view = getChildAt(from);
        removeViewAt(from);
        addView(view, to);
    }

    public void createChildren() {
        ChildViewCreatorIterator it = viewCreationIterator();
        for (; !it.atEnd(); it.next()) {
            View view = it.makeView();
            addView(view);
        }
    }

    protected ChildViewCreatorIterator viewCreationIterator() {
        return new ChildViewCreatorIterator();
    }
}
