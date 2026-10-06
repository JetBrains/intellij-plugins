// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.prettierjs.PrettierImportCodeStyleAction;
import com.intellij.prettierjs.PrettierJSTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class PrettierImportCodeStyleActionTest extends BasePlatformTestCase {
  public void testImportFromConfigFile() {
    doTestImport(".prettierrc.json", "{\"tabWidth\": 3}", 3);
  }

  public void testImportFromConfigFileNextToPackageJson() {
    myFixture.addFileToProject(".prettierrc.json", "{\"tabWidth\": 5}");
    doTestImport("package.json", "{}", 5);
  }

  public void testUnavailableForOtherFile() {
    var file = myFixture.addFileToProject("index.js", "").getVirtualFile();
    var presentation = PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, getProject(), file);
    assertFalse(presentation.isEnabledAndVisible());
  }

  private void doTestImport(String contextFileName, String contextFileText, int expectedIndentSize) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      var file = myFixture.addFileToProject(contextFileName, contextFileText).getVirtualFile();
      var presentation = PrettierJSTestUtil.updateAndPerformAction(PrettierImportCodeStyleAction.ACTION_ID, getProject(), file);
      assertTrue(presentation.isEnabledAndVisible());
      assertEquals(expectedIndentSize, settings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions().INDENT_SIZE);
    });
  }
}
