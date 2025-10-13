// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.resolve;

import com.intellij.usages.Usage;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

public class GherkinStepParameterFindUsagesTest extends CucumberCodeInsightTestCase {
  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/refactoring";
  }

  public void testFindUsages() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    String[] usages = myFixture.testFindUsagesUsingAction("test.feature").stream().map(Usage::toString).toArray(String[]::new);
    assertEquals(4, usages.length);
    String[] expectedUsages = new String[]{
      "",
      "4|Given| there are <|start|> and <|start|> cucumbers",
      "5|Given| there are <|start|> cucumbers",
      "6|Given| there are <|start|> cucumbers",
    };
    assertSameElements(expectedUsages, usages);
  }
}
