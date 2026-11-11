// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit;

import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.linter.JSExternalToolFixture;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.prettierjs.PrettierConfiguration;
import com.intellij.prettierjs.PrettierImportCodeStyleAction;
import com.intellij.prettierjs.PrettierJSTestUtil;
import com.intellij.prettierjs.PrettierUtil;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.linter.JSExternalToolFixture.jsExternalToolFixture;

@TestApplication
public class ImportCodeStyleWithPrettierIntegrationTest {
  private final TestFixture<JSExternalToolFixture> prettier = jsExternalToolFixture(
    new JSTempDirFixtureOptions(PrettierJSTestUtil.getTestDataPath() + "import")
      .withMainPackage(PrettierUtil.PACKAGE_NAME));

  @BeforeEach
  void setUpPrettier() {
    JSExternalToolFixture fixture = prettier.get();
    PrettierConfiguration.getInstance(fixture.getProject())
      .withLinterPackage(NodePackageRef.create(fixture.getNodePackage()))
      .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;
  }

  @Test
  public void testFromJsFile() {
    doTestForFile("/.prettierrc.js", settings ->
      Assertions.assertEquals(7, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE));
  }

  @Test
  public void testFromSharedConfig() {
    doTestForFile("/.prettierrc.json", settings ->
      Assertions.assertEquals(9, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE));
  }

  private void doTestForFile(String fileName, Consumer<CodeStyleSettings> checkResult) {
    JSExternalToolFixture fixture = prettier.get();
    JSTestUtils.testWithTempCodeStyleSettings(fixture.getProject(), settings -> {
      fixture.getFixture().copyDirectoryToProject(fixture.getTestName(true), "");
      var file = fixture.getFixture().configureFromTempProjectFile(fileName).getVirtualFile();
      // The action needs the EDT.
      var presentation = EdtTestUtil.runInEdtAndGet(
        () -> PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, fixture.getProject(), file));
      Assertions.assertTrue(presentation.isEnabledAndVisible());
      checkResult.consume(settings);
    });
  }
}
