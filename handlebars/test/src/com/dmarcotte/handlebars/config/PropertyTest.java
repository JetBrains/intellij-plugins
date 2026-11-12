package com.dmarcotte.handlebars.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyTest {

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
    int expectedNumberOfPropertyFields = 6;


    int length = Property.values().length;
    Field[] fields = Property.class.getDeclaredFields();
    assertEquals(expectedNumberOfPropertyFields,
                 length,
                 "Declared properties in enum \"" +
                 Property.class.getSimpleName() +
                 "\" have changed!  Ensure that changes are backwards compatible " +
                 "and " + PropertyNameTest.class.getName() + " has been updated appropriately.\n" +
                 "Fields: " + Arrays.toString(fields));
  }

  @Test
  public void ensureAllPropertiesAreTested() {
    Set<Property> properties = EnumSet.allOf(Property.class);

    for (PropertyNameTest.PropertyTestDefinition propertyTestDefinition : PropertyNameTest.PROPERTY_TEST_DEFINITIONS) {
      properties.remove(propertyTestDefinition.property);
    }

    assertTrue(properties.isEmpty(),
               "The following " + Property.class.getSimpleName() + " entries do not have corresponding " +
               PropertyNameTest.PropertyTestDefinition.class.getSimpleName() +
               " tests defined: " + properties.toString());
  }

  @Test
  public void testPropertyNameUniqueness() {
    Set<String> propertyNameStrings = new HashSet<>();

    for (Property property : Property.values()) {
      String propertyNameString = property.getStringName();
      assertFalse(propertyNameStrings.contains(propertyNameString),
                  "Property string name \"" + propertyNameString + "\" is not unique in Property");
      propertyNameStrings.add(propertyNameString);
    }
  }
}
