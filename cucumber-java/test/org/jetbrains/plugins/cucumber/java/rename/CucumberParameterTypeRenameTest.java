// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberParameterTypeRenameTest extends CucumberCodeInsightTestCase {
  public void testParameterTypeRename() {
    myFixture.copyDirectoryToProject("", "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("ParameterTypeSteps.java"));

    assertTrue("File should contain definition and usages of ParameterType 'iso-date'", myFixture.getFile().getText().contains("iso-date"));

    final int offset = findOffsetBySignature("today is {iso-d<caret>ate}");
    PsiElement parameterType = myFixture.getFile().findReferenceAt(offset).resolve();

    myFixture.renameElement(parameterType, "new-iso-value");
    assertFalse("ParameterType and all its references should be renamed", myFixture.getFile().getText().contains("iso-date"));
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "parameterTypeRename";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
  }
}
