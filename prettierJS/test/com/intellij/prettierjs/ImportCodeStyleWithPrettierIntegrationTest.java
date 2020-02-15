// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.util.Consumer;
import org.junit.Assert;

public class ImportCodeStyleWithPrettierIntegrationTest extends JSExternalToolIntegrationTest {
  @Override
  protected String getMainPackageName() {
    return PrettierUtil.PACKAGE_NAME;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "import");
    PrettierConfiguration.getInstance(getProject())
      .withInterpreterRef(getNodeInterpreter())
      .withLinterPackage(NodePackageRef.create(getNodePackage()));
  }

  public void testFromJsFile() {
    doTestForFile("/.prettierrc.js", settings ->
      Assert.assertEquals(7, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE));
  }

  public void testFromSharedConfig() {
    doTestForFile("/.prettierrc.json", settings ->
      Assert.assertEquals(9, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE));
  }

  private void doTestForFile(String fileName, Consumer<CodeStyleSettings> checkResult) {
    JSTestUtils.testWithTempCodeStyleSettings(myFixture.getProject(), settings -> {
      myFixture.copyDirectoryToProject(getTestName(true), "");
      myFixture.configureFromTempProjectFile(fileName);
      myFixture.performEditorAction(PrettierImportCodeStyleAction.ACTION_ID);
      checkResult.consume(settings);
    });
  }
}
