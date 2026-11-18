// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierImportCodeStyleAction;
import com.intellij.prettierjs.PrettierJSTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class PrettierImportCodeStyleActionTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @Test
  public void testImportFromConfigFile() {
    doTestImport(".prettierrc.json", "{\"tabWidth\": 3}", 3);
  }

  @Test
  public void testImportFromConfigFileNextToPackageJson() {
    codeInsight.get().addFileToProject(".prettierrc.json", "{\"tabWidth\": 5}");
    doTestImport("package.json", "{}", 5);
  }

  @Test
  public void testUnavailableForOtherFile() {
    CodeInsightTestFixture fixture = codeInsight.get();
    var file = fixture.addFileToProject("index.js", "").getVirtualFile();
    var presentation = runInEdtAndGet(
      () -> PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, fixture.getProject(), file));
    assertFalse(presentation.isEnabledAndVisible());
  }

  private void doTestImport(String contextFileName, String contextFileText, int expectedIndentSize) {
    CodeInsightTestFixture fixture = codeInsight.get();
    JSTestUtils.testWithTempCodeStyleSettings(fixture.getProject(), settings -> {
      var file = fixture.addFileToProject(contextFileName, contextFileText).getVirtualFile();
      var presentation = runInEdtAndGet(
        () -> PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, fixture.getProject(), file));
      assertTrue(presentation.isEnabledAndVisible());
      assertEquals(expectedIndentSize, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE);
    });
  }
}
