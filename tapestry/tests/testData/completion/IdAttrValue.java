package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.PageLink;

import java.util.Date;

public class IdAttrValue {

	@Component(id = "index", parameters = {"page=Index"})
	private PageLink index55;

	@Component(id = "link2", parameters = {"page=Index"})
	private PageLink link2;

	@Component(parameters = {"page=Index"})
	private PageLink link3;

    public String getCurrDate() {
        return new Date().toString();
    }
}