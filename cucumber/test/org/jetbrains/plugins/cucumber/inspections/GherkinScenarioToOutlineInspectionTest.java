// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.util.List;

public class GherkinScenarioToOutlineInspectionTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testScenarioWithExamples() {
    myFixture.enableInspections(GherkinScenarioToScenarioOutlineInspection.class);

    myFixture.configureByText(
      GherkinFileType.INSTANCE,
      "Feature: test\n" +
      "  <error descr=\"Only Scenario Outline could have an Examples\">Scenario</error>: b\n" +
      "    Given step\n" +
      "    Examples:\n" +
      "      | target         | version |\n" +
      "      | Object.desc    | <0.9.0  |"
    );

    myFixture.checkHighlighting(false, false, false);

    List<IntentionAction> fixes = myFixture.getAllQuickFixes();
    IntentionAction quickFix= CodeInsightTestUtil.findIntentionByText(fixes, "Convert Scenario to Scenario Outline");
    myFixture.launchAction(quickFix);

    myFixture.checkResult(
      "Feature: test\n" +
      "\n" +
      "  Scenario Outline: b\n" +
      "    Given step\n" +
      "    Examples:\n" +
      "      | target      | version |\n" +
      "      | Object.desc | <0.9.0  |"
    );
  }
}
