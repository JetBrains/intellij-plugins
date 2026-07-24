// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

import java.util.List;

public class CucumberExamplesColonInspectionTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(CucumberExamplesColonInspection.class);
  }

  public void testMissingColon() {
    myFixture.testHighlighting(false, false, false, getTestName(true) + ".feature");
  }

  public void testCorrectExamples() {
    myFixture.testHighlighting(false, false, false, getTestName(true) + ".feature");
  }

  public void testQuickFix() {
    String name = getTestName(true);
    myFixture.configureByFile(name + ".feature");

    List<IntentionAction> fixes = myFixture.getAllQuickFixes();
    IntentionAction quickFix = CodeInsightTestUtil.findIntentionByText(fixes, "Add missing ':' after examples keyword");
    assertNotNull(quickFix);
    myFixture.launchAction(quickFix);

    myFixture.checkResultByFile(name + ".after.feature");
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/inspections/examplesColon";
  }
}
