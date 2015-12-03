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
public abstract class DynamicViewGroup extends ScopedViewGroup {

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

    private boolean isDirty = false;
    private int startViewIndex = 0;

    /**
     * TODO
     */
    private List<ViewFactory> childDefinitions = new ArrayList<>();

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
    public void createChildren() {
        isDirty = false;

        if (childDefinitions.isEmpty()) {
            return;
        }

        removeAllViews();

        ChildViewCreatorIterator it = viewCreationIterator();
        for (int i = 0; i < startViewIndex; ++i) {
            if (it.hasNext()) {
                it.next();
            } else {
                break;
            }
        }

        for (; it.hasNext(); it.next()) {
            View childView = it.makeView();
            addView(childView);
        }

            // TODO: only add Views if they are in the viewport. HOW TO DO THISSSS??? let's do it in boxmodelnode.
            //       here, we'll create a child view iterator? and iterate forward? use c++ forward iterator, not Iterator<>.
            //       we go until hasNext() returns false. boxmodelnode can create it's own iterator that will stop if
            //       we go out of the viewport. this way, it can handle all scroll related stuff.

            /* NEXT TODO:
             * 1) instead of this loop here, use an iterator and make sure it works (both activities)
             * 2) move child view creation to onMeasure() (LinearLayout + Container) and make sure it all works
             * 3) move child measurement to iterator and make sure it all works
             * 4) remove createChildren() and instead make views call removeAllViews()
             * ...
             */
    }

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }

    public void markDirty() {
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        // TODO: what do we do here?
        /* so we have to accomplish:
         * - scrollbar awakening if: (EASY)
         *   * scrolling is enabled
         *   * if height/width of contents of view is greater than height/width of view
         * - do not create/size EVERY view. we only want to size & layout views that are visible.
         *   * so we have to measure views as they are created?
         * - on scroll change, start from an edge and remove views that are no longer in view, then add views that are in view
         * - on recreate views, only create views that are in view
         *
         * conclusions:
         * - so, let's keep track of the top-left view, that way we can easily do child recreation.
         *   * on scroll change, we change the top left accordingly.
         * - if scrolling is enabled, then children are measured w/ UNSPECIFIED maximum, ALWAYS.
         *
         * FIXME: w/ this approach, scopes for views that are not visible will not have their watches/events executed.
         *        this is probably an issue, needsto be fixed.
         */
    }

    protected ChildViewCreatorIterator viewCreationIterator() {
        return new ChildViewCreatorIterator();
    }
}
