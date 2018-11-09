package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.PageLink;

import java.util.Date;

public class AttrValueWithPropPrefix {

	@Component(parameters = {"page=Index"})
	private PageLink link3;

    public Date getDateProp() {
        return null;
    }

    public String getStrProp() {
        return null;
    }

    public Integer getIntProp() {
        return 0;
    }
}