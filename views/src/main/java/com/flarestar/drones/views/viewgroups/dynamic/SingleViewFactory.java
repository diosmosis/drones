package com.flarestar.drones.views.viewgroups.dynamic;

import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.ViewFactory;

public abstract class SingleViewFactory implements ViewFactory {

    public class Iterator implements ViewFactory.Iterator {
        private boolean created = false;

        @Override
        public boolean hasNext() {
            return !created;
        }

        @Override
        public void next() {
            throw new UnsupportedOperationException("next() should not be called in SingleViewFactory.Iterator");
        }

        @Override
        public View makeView() {
            if (created) {
                throw new IllegalStateException("view has already been created");
            }

            created = true;

            return SingleViewFactory.this.makeView();
        }
    }

    public abstract View makeView();

    @Override
    public ViewFactory.Iterator iterator(ViewGroup parent) {
        return new Iterator();
    }
}
