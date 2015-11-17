package com.flarestar.drones.views;

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

    public Row(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isHorizontal = true;
    }
}
