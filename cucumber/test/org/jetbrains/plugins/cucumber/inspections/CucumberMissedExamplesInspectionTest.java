// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

import java.util.List;

public class CucumberMissedExamplesInspectionTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(CucumberMissedExamplesInspection.class);
  }

  public void testScenarioOutlineWithMissedExamples() {
    myFixture.testHighlighting(false, false, false, getTestName(true) + ".feature");
  }

  public void testCorrectScenarioOutline() {
    myFixture.testHighlighting(false, false, false, getTestName(true) + ".feature");
  }

  public void testQuickFixIfSeveralStepsWithParams() {
    doQuickFixTest();
  }

  public void testQuickFixIfStepHasSeveralSubstitutions() {
    doQuickFixTest();
  }

  public void testQuickFixIfSubstitutionWasMentionedSeveralTimes() {
    doQuickFixTest();
  }

  public void testQuickFixForSubstitutionWithoutName() {
    doQuickFixTest();
  }

  public void testQuickFixForOutlineWithoutSubstitutions() {
    doQuickFixTest();
  }

  public void testQuickFixForSubstitutionWithSymbolCharacters() {
    doQuickFixTest();
  }

  public void testQuickFixForSubstitutionInMultilineArgument() {
    doQuickFixTest();
  }

  public void testQuickFixForNotSubstitutionInMultilineArgument() {
    doQuickFixTest();
  }

  public void testQuickFixForInternationalizedFeature() {
    doQuickFixTest();
  }

  private void doQuickFixTest() {
    String name = getTestName(true);
    myFixture.configureByFile(name + ".feature");

    List<IntentionAction> fixes = myFixture.getAllQuickFixes();
    IntentionAction quickFix = CodeInsightTestUtil.findIntentionByText(fixes, "Create Examples Section");
    assertNotNull(quickFix);
    myFixture.launchAction(quickFix);

    myFixture.checkResultByFile(name + ".after.feature");
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/inspections/missedExamples";
  }
}
