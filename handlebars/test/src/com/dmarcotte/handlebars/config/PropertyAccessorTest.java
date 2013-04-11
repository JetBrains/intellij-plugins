package com.dmarcotte.handlebars.config;

import junit.framework.Assert;
import org.junit.Test;

public class PropertyAccessorTest {

  // grab a Property to use in this test.  NOTE: the specific property is not significant.
  private final Property myTestProperty = Property.FORMATTER;

  @Test
  public void testGetPropertyValue() {
    PropertiesComponentStub propertiesComponent = new PropertiesComponentStub();
    String originalValue = Property.DISABLED;

    // simulate an existing value by setting it directly on the propertiesComponent
    propertiesComponent.setValue(myTestProperty.getStringName(), originalValue);

    String propertyValue = new PropertyAccessor(propertiesComponent).getPropertyValue(myTestProperty);

    Assert.assertEquals("Problem fetching existing property", originalValue, propertyValue);
  }

  @Test
  public void testGetPropertyValueDefaulting() {
    PropertiesComponentStub propertiesComponent = new PropertiesComponentStub();

    String expectedValue = myTestProperty.getDefault();
    String propertyValue = new PropertyAccessor(propertiesComponent).getPropertyValue(myTestProperty);

    Assert.assertEquals("Default value should have been returned", expectedValue, propertyValue);
  }

  @Test
  public void testSetPropertyValue() {
    PropertiesComponentStub propertiesComponent = new PropertiesComponentStub();

    String testValue = Property.DISABLED;
    new PropertyAccessor(propertiesComponent).setPropertyValue(myTestProperty, Property.DISABLED);

    // fetch the value directly to ensure PropertyAccessor didn't mess it up
    String propertiesComponentValue = propertiesComponent.getValue(myTestProperty.getStringName());

    Assert.assertEquals("Value was not properly persisted", testValue, propertiesComponentValue);
  }
}
