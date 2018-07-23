package com.testapp.components;

import org.apache.tapestry5.annotations.Property;

import java.util.Date;

public class TapestryAttrValue {

    @Property
    private int intFieldProp;

    private long longFieldProp;

    public Date getDateProp() {
        return null;
    }

    public String getStrProp() {
        return null;
    }
}