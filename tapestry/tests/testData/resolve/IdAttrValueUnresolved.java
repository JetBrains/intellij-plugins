package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.PageLink;

import java.util.Date;

public class IdAttrValueUnresolved {

	// This provides the invisible instrumentation of the 3rd page link.
	@Component(parameters = {"page=Index"})
	private PageLink index33;

    public String getCurrDate() {
        return new Date().toString();
    }
}