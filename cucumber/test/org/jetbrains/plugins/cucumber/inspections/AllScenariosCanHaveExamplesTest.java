// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

/** Since Gherkin 6 every scenario keyword works the same: Examples make it an outline, the keyword does not. */
public class AllScenariosCanHaveExamplesTest extends BasePlatformTestCase {

  // An unused Examples column is reported, whatever the scenario keyword.
  public void testUnusedColumnInScenario() { checkUnusedColumn("Scenario"); }
  public void testUnusedColumnInExample() { checkUnusedColumn("Example"); }
  public void testUnusedColumnInScenarioOutline() { checkUnusedColumn("Scenario Outline"); }
  public void testUnusedColumnInScenarioTemplate() { checkUnusedColumn("Scenario Template"); }

  // A <placeholder> with no Examples to fill it is reported, whatever the scenario keyword.
  public void testPlaceholderWithoutExamplesInScenario() { checkPlaceholderWithoutExamples("Scenario"); }
  public void testPlaceholderWithoutExamplesInExample() { checkPlaceholderWithoutExamples("Example"); }
  public void testPlaceholderWithoutExamplesInScenarioOutline() { checkPlaceholderWithoutExamples("Scenario Outline"); }
  public void testPlaceholderWithoutExamplesInScenarioTemplate() { checkPlaceholderWithoutExamples("Scenario Template"); }

  // No placeholder and no Examples is a normal scenario, whatever the keyword.
  public void testNormalScenario() { checkNormalScenario("Scenario"); }
  public void testNormalExample() { checkNormalScenario("Example"); }
  public void testNormalScenarioOutline() { checkNormalScenario("Scenario Outline"); }
  public void testNormalScenarioTemplate() { checkNormalScenario("Scenario Template"); }

  private void checkUnusedColumn(String keyword) {
    myFixture.enableInspections(CucumberTableInspection.class);
    myFixture.configureByText(GherkinFileType.INSTANCE,
      "Feature: test\n" +
      "  " + keyword + ": b\n" +
      "    Given status is <code>\n" +
      "    Examples:\n" +
      "      | code | <warning descr=\"Unused table column\">extra</warning> |\n" +
      "      | 200  | x     |\n");
    myFixture.checkHighlighting(true, false, false);
  }

  private void checkPlaceholderWithoutExamples(String keyword) {
    myFixture.enableInspections(CucumberMissedExamplesInspection.class);
    myFixture.configureByText(GherkinFileType.INSTANCE,
      "Feature: test\n" +
      "  <error descr=\"Examples section is missing\">" + keyword + "</error>: b\n" +
      "    Given status is <code>\n");
    myFixture.checkHighlighting(false, false, false);
  }

  private void checkNormalScenario(String keyword) {
    myFixture.enableInspections(CucumberMissedExamplesInspection.class);
    myFixture.configureByText(GherkinFileType.INSTANCE,
      "Feature: test\n" +
      "  " + keyword + ": b\n" +
      "    Given status is 200\n");
    myFixture.checkHighlighting(false, false, false);
  }
}
