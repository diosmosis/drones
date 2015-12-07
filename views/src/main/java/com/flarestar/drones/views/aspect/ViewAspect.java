package com.flarestar.drones.views.aspect;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;

public abstract class ViewAspect {

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // empty
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public void checkScrollPositionDuringDraw() {
        // empty
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        // empty
    }

    public void manipulateScrollToCoords(Point tempPoint) {
        // empty
    }

    public void onDraw(Canvas canvas) {
        // empty
    }
}
