package com.dmarcotte.handlebars.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

@RunWith(value = Parameterized.class)
public class PropertyTest2 {

  private final PropertyTestDefinition propertyTestDefinition;

  static final List<PropertyTestDefinition> PROPERTY_TEST_DEFINITIONS = new ArrayList<PropertyTestDefinition>();

  static {
    PROPERTY_TEST_DEFINITIONS.add(new PropertyTestDefinition(Property.AUTO_GENERATE_CLOSE_TAG,
                                                             "HbDisableAutoGenerateCloseTag"));

    PROPERTY_TEST_DEFINITIONS.add(new PropertyTestDefinition(Property.FORMATTER,
                                                             "HbFormatter"));

    PROPERTY_TEST_DEFINITIONS.add(new PropertyTestDefinition(Property.AUTO_COLLAPSE_BLOCKS,
                                                             "HbAutoCollapseBlocks"));

    PROPERTY_TEST_DEFINITIONS.add(new PropertyTestDefinition(Property.COMMENTER_LANGUAGE_ID,
                                                             "HbCommenterLanguageId"));
  }

  @Parameterized.Parameters
  public static List<Object[]> parameters() {
    List<Object[]> testParameters = new ArrayList<Object[]>();
    for (PropertyTestDefinition propertyTestDefinition : PROPERTY_TEST_DEFINITIONS) {
      testParameters.add(new Object[]{propertyTestDefinition});
    }

    return testParameters;
  }

  public PropertyTest2(PropertyTestDefinition propertyTestDefinition) {
    this.propertyTestDefinition = propertyTestDefinition;
  }

  @Test
  public void testPropertyNameBackwardsCompatibility() {

    Assert.assertEquals("Error in " +
                        propertyTestDefinition.property.name() +
                        ".\n\tPersisted property name changed.  This will mess up user preferences without some sort of migration strategy.\n\n",
                        propertyTestDefinition.expectedPropertyName,
                        propertyTestDefinition.property.getStringName());
  }

  /**
   * Associates a {@link Property} with its expected attributes to ensure stability and backwards compatibility
   */
  static class PropertyTestDefinition {
    final Property property;
    final String expectedPropertyName;

    PropertyTestDefinition(Property property, String expectedPropertyName) {
      this.property = property;
      this.expectedPropertyName = expectedPropertyName;
    }
  }
}
