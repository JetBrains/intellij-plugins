// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

public class JadeFormatterTest extends LightPlatformCodeInsightTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/formatter/";
  }

  public void testTags() {
    doTest();
  }

  public void testTags2() {
    doTestWithIndentOptions(2, 4, false);
  }

  public void testTags3() {
    doTest();
  }

  public void testMultilineAttributes() {
    doTest();
  }

  public void testIncludeWithInternals() {
    doTest();
  }

  public void testLeadingComment() {
    doTest();
  }

  public void testScriptStyle() {
    doTest();
  }

  public void testSpacesToTabs() {
    doTestWithIndentOptions(4, 4, true);
  }

  public void testIndentsInTextBlock() {
    doTest();
  }

  public void testComment() {
    doTest();
  }

  public void testScript2() {
    doTest();
  }

  public void testEmbeddedHtmlPlainText() {
    doTest();
  }

  public void testSeveralJavascripts() {
    doTest();
  }

  public void testWeb14016() {
    final String fileName = getTestName(true) + ".jade";

    configureByFile(fileName);
    doReformat(getFile());
    checkResultByFile(fileName);
  }

  public void testText() {
    doTestWithIndentOptions(4, 4, true);
  }

  private void doTest() {
    configureByFile(getTestName(true) + ".jade");
    doReformat(getFile());
    checkResultByFile(getTestName(true) + "_after.jade");
  }

  private void doReformat(final PsiElement file) {
    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
    ApplicationManager.getApplication().runWriteAction(() -> {
      codeStyleManager.reformat(file);
    });
  }

  private void doTestWithIndentOptions(int indentSize, int tabSize, boolean useTabs) {
    CodeStyleSettings currSettings = CodeStyle.getSettings(getProject());
    CodeStyleSettings tempSettings = CodeStyle.createTestSettings(currSettings);
    CommonCodeStyleSettings.IndentOptions indentOptions = tempSettings.getCommonSettings(JadeLanguage.INSTANCE).getIndentOptions();
    CommonCodeStyleSettings.IndentOptions jsIndentOptions = tempSettings.getCommonSettings(JavascriptLanguage.INSTANCE).getIndentOptions();
    assert indentOptions != null;
    assert jsIndentOptions != null;
    indentOptions.INDENT_SIZE = indentSize;
    indentOptions.TAB_SIZE = tabSize;
    indentOptions.USE_TAB_CHARACTER = useTabs;
    jsIndentOptions.INDENT_SIZE = indentSize;
    jsIndentOptions.TAB_SIZE = tabSize;
    jsIndentOptions.USE_TAB_CHARACTER = useTabs;
    CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(tempSettings);
    try {
      doTest();
    }
    finally {
      CodeStyleSettingsManager.getInstance(getProject()).dropTemporarySettings();
    }
  }

  public void testUnbufferedSameIndent() {
    doTestWithIndentOptions(4, 4, false);
  }
}
