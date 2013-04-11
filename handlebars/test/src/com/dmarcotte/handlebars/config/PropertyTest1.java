package com.dmarcotte.handlebars.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PropertyTest1 {

  /**
   * This test will fail if properties are added/removed in {@link com.dmarcotte.handlebars.config.Property}
   * <p/>
   * When/if it fails:
   * - ensure the change is backwards compatible (i.e. when users upgrade, their properties will still be in the same state)
   * - update this test with the new number of properties to get it passing
   */
  @Test
  public void testPropertiesChange() {
    // expectedNumberOfPropertyFields represents the number of enum entries plus that static members, plus one for the $VALUES that every enum gets
    int expectedNumberOfPropertyFields = 7;

    Assert.assertEquals("Declared properties in enum \"" +
                        Property.class.getSimpleName() +
                        "\" have changed!  Ensure that changes are backwards compatible " +
                        "and com.dmarcotte.handlebars.config.PropertyTest2 has been updated appropriately.\n",
                        expectedNumberOfPropertyFields,
                        Property.class.getDeclaredFields().length);
  }

  @Test
  public void ensureAllPropertiesAreTested() {
    Set<Property> properties = new HashSet<Property>(Arrays.asList(Property.values()));

    for (PropertyTest2.PropertyTestDefinition propertyTestDefinition : PropertyTest2.PROPERTY_TEST_DEFINITIONS) {
      properties.remove(propertyTestDefinition.property);
    }

    Assert.assertTrue("The following " + Property.class.getSimpleName() + " entries do not have corresponding " +
                      PropertyTest2.PropertyTestDefinition.class.getSimpleName() +
                      " tests defined: " + properties.toString(),
                      properties.isEmpty());
  }

  @Test
  public void testPropertyNameUniqueness() {
    Set<String> propertyNameStrings = new HashSet<String>();

    for (Property property : Property.values()) {
      String propertyNameString = property.getStringName();
      junit.framework.Assert.assertFalse("Property string name \"" + propertyNameString + "\" is not unique in Property",
                                         propertyNameStrings.contains(propertyNameString));
      propertyNameStrings.add(propertyNameString);
    }
  }
}
