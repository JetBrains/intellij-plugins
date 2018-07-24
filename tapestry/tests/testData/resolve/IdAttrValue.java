package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.PageLink;

import java.util.Date;

public class IdAttrValue {

	// This provides the invisible instrumentation of the 3rd page link.
	@Component(id = "index", parameters = {"page=Index"})
	private PageLink index55;

    public String getCurrDate() {
        return new Date().toString();
    }
}