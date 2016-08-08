package com.dmarcotte.handlebars.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(value = Parameterized.class)
public class PropertyNameTest {
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

  static final List<PropertyTestDefinition> PROPERTY_TEST_DEFINITIONS = Arrays.asList(
    new PropertyTestDefinition(Property.AUTO_GENERATE_CLOSE_TAG, "HbDisableAutoGenerateCloseTag"),
    new PropertyTestDefinition(Property.AUTOCOMPLETE_MUSTACHES, "HbAutocompleteMustaches"),
    new PropertyTestDefinition(Property.FORMATTER, "HbFormatter"),
    new PropertyTestDefinition(Property.AUTO_COLLAPSE_BLOCKS, "HbAutoCollapseBlocks"),
    new PropertyTestDefinition(Property.COMMENTER_LANGUAGE_ID, "HbCommenterLanguageId"),
    new PropertyTestDefinition(Property.SHOULD_OPEN_HTML, "HbShouldOpenHtmlAsHb"));

  @Parameterized.Parameters
  public static List<Object[]> parameters() {
    List<Object[]> testParameters = new ArrayList<>();
    for (PropertyTestDefinition propertyTestDefinition : PROPERTY_TEST_DEFINITIONS) {
      testParameters.add(new Object[]{propertyTestDefinition});
    }

    return testParameters;
  }

  private final PropertyTestDefinition propertyTestDefinition;

  public PropertyNameTest(PropertyTestDefinition propertyTestDefinition) {
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
}
