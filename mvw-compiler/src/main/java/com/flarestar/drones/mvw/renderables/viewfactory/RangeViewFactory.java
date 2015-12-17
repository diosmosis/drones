package com.flarestar.drones.mvw.renderables.viewfactory;

import com.flarestar.drones.mvw.renderables.makeview.MakeViewBody;

import javax.lang.model.type.TypeMirror;

/**
 * TODO
 */
public class RangeViewFactory extends ViewFactory {
    private TypeMirror valueType;
    private String getCollectionCode;
    private String getItemCode;
    private String setScopePropertiesCode;

    public RangeViewFactory(MakeViewBody makeViewBody, TypeMirror valueType, String getCollectionCode,
                            String getItemCode, String setScopePropertiesCode) {
        super(makeViewBody);

        this.valueType = valueType;
        this.getCollectionCode = getCollectionCode;
        this.getItemCode = getItemCode;
        this.setScopePropertiesCode = setScopePropertiesCode;
    }

    @Override
    public String getTemplate() {
        return "templates/rangeViewFactory.twig";
    }

    public TypeMirror getValueType() {
        return valueType;
    }

    public String getGetCollectionCode() {
        return getCollectionCode;
    }

    public String getGetItemCode() {
        return getItemCode;
    }

    public String getSetScopePropertiesCode() {
        return setScopePropertiesCode;
    }
}
