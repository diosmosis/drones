package com.flarestar.drones.mvw.view;

/**
 * TODO
 */
public class ViewProperty {
    private String viewMethodName;
    private String[] viewMethodArgs;

    public ViewProperty(String viewMethodName, String... viewMethodArgs) {
        this.viewMethodName = viewMethodName;
        this.viewMethodArgs = viewMethodArgs;
    }

    public String[] getViewMethodArgs() {
        return viewMethodArgs;
    }

    public String getViewMethodName() {
        return viewMethodName;
    }
}
