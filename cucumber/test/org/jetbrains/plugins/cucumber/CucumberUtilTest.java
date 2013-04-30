
package org.jetbrains.plugins.cucumber;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CucumberUtilTest {
  @Test
  public void testFeatureOrFolderNameGetterPositiveScenarios() {
    String featureFilePath = CucumberUtil.getFeatureFileOrFolderNameFromParameters("\"C:/projects/java-calculator/src/test/resources/cucumber/examples/java/calculator/shopping testcase.feature\" --format org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome");
    assertEquals("C:/projects/java-calculator/src/test/resources/cucumber/examples/java/calculator/shopping testcase.feature", featureFilePath);

    featureFilePath = CucumberUtil.getFeatureFileOrFolderNameFromParameters("C:/projects/java-calculator/src/test/resources/cucumber/examples/java/calculator/shopping.feature --format org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome");
    assertEquals("C:/projects/java-calculator/src/test/resources/cucumber/examples/java/calculator/shopping.feature", featureFilePath);
  }

  @Test
  public void testFeatureOrFolderNameGetterNegativeScenarios() {
    String featureFilePath = CucumberUtil.getFeatureFileOrFolderNameFromParameters("\"C:/projects/java --format org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome");
    assertEquals(null, featureFilePath);

    featureFilePath = CucumberUtil.getFeatureFileOrFolderNameFromParameters("");
    assertEquals(null, featureFilePath);
  }
}
