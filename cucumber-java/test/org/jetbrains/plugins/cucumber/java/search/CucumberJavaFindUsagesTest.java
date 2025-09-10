// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.search;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.usages.Usage;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

import java.util.Collection;

public class CucumberJavaFindUsagesTest extends CucumberJavaCodeInsightTestCase {
  public void testStepUsages() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    Collection<Usage> usages = myFixture.testFindUsagesUsingAction("Steps.java", "test.feature");
    assertEquals(3, usages.size());
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "search";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }
}
