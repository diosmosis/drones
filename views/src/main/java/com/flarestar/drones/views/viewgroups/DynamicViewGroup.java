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
         *
         * NEXT STEPS:
         * - for scrolling, we need an incremental layout algorithm. but for not scrolling, we need to go over every view. how do we organize this?
         *   * ok, here's an idea: let's create some measuring/layout strategies. they will be done independently of android's layout procedure.
         *     when we do onMeasure, we create views & do real layout. in onLayout, we just call `child.layout(...)` using properties stored in LayoutParams.
         *   * also, we have to handle scrolling in LinearLayout & Container. we can't do it generically in the base type, since it will be different for
         *     each subtype.
         *
         * 1. move view creation from BoxModelNode to Container/Layout
         * 2. perform measure + layout in Container/Layout's onMeasure. store top/left in boxmodelnode layout params. then in onLayout, just call child.layout(...).
         * 3. handle scrolling in Container/LinearLayout. start w/ Container (test, then move on to LinearLayout).
         *    - if view is out of viewport, do not measure/layout.
         *    - remember, there are two cases: when there are no views and they have to be created and when there are views because a child view's views have been removed.
         * 4. handle scroll change. should be in Container/LinearLayout. start w/ Container again.
         */
    }

    protected ChildViewCreatorIterator viewCreationIterator() {
        return new ChildViewCreatorIterator();
    }
}
