// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit;

import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest;
import com.intellij.prettierjs.PrettierConfiguration;
import com.intellij.prettierjs.PrettierImportCodeStyleAction;
import com.intellij.prettierjs.PrettierJSTestUtil;
import com.intellij.prettierjs.PrettierUtil;
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
      .withLinterPackage(NodePackageRef.create(getNodePackage()))
      .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;
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
      var file = myFixture.configureFromTempProjectFile(fileName).getVirtualFile();
      var presentation = PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, getProject(), file);
      Assert.assertTrue(presentation.isEnabledAndVisible());
      checkResult.consume(settings);
    });
  }
}
