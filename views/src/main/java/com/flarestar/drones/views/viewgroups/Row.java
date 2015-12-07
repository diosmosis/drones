package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;

/**
 * TODO
 */
public class Row extends LinearLayout {
    public Row(Context context) {
        super(context);

        isHorizontal = true;
    }

    public Row(Context context, AttributeSet attrs) {
        super(context, attrs);

        isHorizontal = true;
    }
}
