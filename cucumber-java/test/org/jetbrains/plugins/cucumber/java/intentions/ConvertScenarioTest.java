// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.intentions;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;

import static com.intellij.testFramework.fixtures.CodeInsightTestUtil.doIntentionTest;

public class ConvertScenarioTest extends BaseCucumberJavaResolveTest {
  public void testOptionalParameter() {
    doTest();
  }

  public void testTheSameParameterNamesInDifferentSteps() {
    doTest();
  }

  private void doTest() {
    String projectDirectory = getTestName(true);
    myFixture.copyDirectoryToProject(projectDirectory, "");
    String intentionName = CucumberBundle.message("intention.convert.scenario.to.outline.name");
    doIntentionTest(myFixture, intentionName, projectDirectory + "/test.feature", projectDirectory + "/test_after.feature");
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "intentions";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberJava8ProjectDescriptor();
  }
}
