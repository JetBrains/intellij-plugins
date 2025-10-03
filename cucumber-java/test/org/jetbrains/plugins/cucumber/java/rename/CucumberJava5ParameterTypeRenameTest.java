// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterTypeReference;

/// [cucumber-jvm](https://github.com/cucumber/cucumber-jvm) had major changes to package names in v5.0.0
/// ([learn more](https://github.com/cucumber/cucumber-jvm/blob/main/release-notes/v5.0.0.md#new-package-structure)).
///
/// @see CucumberJavaParameterTypeReference
public class CucumberJava5ParameterTypeRenameTest extends CucumberCodeInsightTestCase {
  private void doTest(String newName) {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");
    myFixture.configureByFiles("Steps.java");
    myFixture.testHighlighting("Steps.java"); // ensure everything is resolved

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("Steps.java", getTestName(true) + "/after/Steps.java", false);
  }

  public void testDefinedWithTypeRegistry() {
    doTest("new-iso-value");
  }

  public void testDefinedWithAnnotation() {
    doTest("newMoodName");
  }

  public void testDefinedWithAnnotationNameAttribute() {
    doTest("newMoodName");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameParameterType/parameterTypeRename_5";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber5ProjectDescriptor();
  }
}
