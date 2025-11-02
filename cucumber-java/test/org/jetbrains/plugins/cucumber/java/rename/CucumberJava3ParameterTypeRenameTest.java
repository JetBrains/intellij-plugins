// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterTypeReference;

/// @see CucumberJavaParameterTypeReference
public class CucumberJava3ParameterTypeRenameTest extends CucumberCodeInsightTestCase {
  private void doTest(String newName) {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");
    myFixture.configureByFile("Steps.java");
    myFixture.testHighlighting("Steps.java"); // ensure everything is resolved

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("Steps.java", getTestName(true) + "/after/Steps.java", false);
  }

  public void testDefinedWithTypeRegistry() {
    doTest("new-iso-value");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameParameterType/parameterTypeRename_3";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
  }
}
