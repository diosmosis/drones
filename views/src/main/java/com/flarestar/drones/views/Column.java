package com.flarestar.drones.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * TODO
 */
public class Column extends LinearLayout {
    public Column(Context context) {
        super(context);

        isHorizontal = false;
    }

    public Column(Context context, AttributeSet attrs) {
        super(context, attrs);

        isHorizontal = false;
    }

    public Column(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isHorizontal = false;
    }
}
