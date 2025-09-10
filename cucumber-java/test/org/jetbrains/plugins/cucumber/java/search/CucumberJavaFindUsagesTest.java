// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.search;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.usages.Usage;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaFindUsagesTest extends CucumberJavaCodeInsightTestCase {
  public void testStepUsages() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    String[] usages = myFixture.testFindUsagesUsingAction("Steps.java").stream().map(Usage::toString).toArray(String[]::new);
    assertEquals(3, usages.length);
    String[] expectedUsages = new String[]{
      "4|Given| |I am happy",
      "6|Then| |I am happy",
      "9|Given| |I am happy"
    };
    assertSameElements(expectedUsages, usages);
  }

  public void testParameterTypeUsages() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    String[] usages = myFixture.testFindUsagesUsingAction("Steps.java").stream().map(Usage::toString).toArray(String[]::new);
    assertEquals(3, usages.length); // Should actually be 4. See IDEA-379147.
    String[] expectedUsages = new String[]{
      "", // Because of 2 usages on line 23
      "19|@And(|\"today is {|isoDate|}\"|)",
      "23|@And(|\"yesterday was {|isoDate|}, before was {|isoDate|}\"|)"
    };
    assertSameElements(expectedUsages, usages);
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
