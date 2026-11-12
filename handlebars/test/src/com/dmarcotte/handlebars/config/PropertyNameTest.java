package com.dmarcotte.handlebars.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

  public static List<Object[]> parameters() {
    List<Object[]> testParameters = new ArrayList<>();
    for (PropertyTestDefinition propertyTestDefinition : PROPERTY_TEST_DEFINITIONS) {
      testParameters.add(new Object[]{propertyTestDefinition});
    }

    return testParameters;
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testPropertyNameBackwardsCompatibility(PropertyTestDefinition propertyTestDefinition) {
    assertEquals(propertyTestDefinition.expectedPropertyName,
                 propertyTestDefinition.property.getStringName(),
                 "Error in " +
                 propertyTestDefinition.property.name() +
                 ".\n\tPersisted property name changed.  This will mess up user preferences without some sort of migration strategy.\n\n");
  }
}
