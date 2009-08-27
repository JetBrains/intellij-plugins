package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.PageLink;
import org.apache.tapestry5.annotations.Property;

import java.util.Date;

public class TapestryAttrValueWithPropPrefix {

	  // This provides the invisible instrumentation of the 3rd page link.
	  @Component(id = "index", parameters = {"page=Index"})
  	private PageLink index55;

    @Property
    private int intFieldProp;

    private long longFieldProp;

    public Date getDateProp() {
        return null;
    }
}