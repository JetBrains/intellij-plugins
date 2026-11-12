// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@Disabled("AT-4013")
@TestApplication
public class JadeFormatterTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/formatter/");
  }

  @Test
  public void testTags() {
    doTest();
  }

  @Test
  public void testTags2() {
    doTestWithIndentOptions(2, 4, false);
  }

  @Test
  public void testTags3() {
    doTest();
  }

  @Test
  public void testMultilineAttributes() {
    doTest();
  }

  @Test
  public void testIncludeWithInternals() {
    doTest();
  }

  @Test
  public void testLeadingComment() {
    doTest();
  }

  @Test
  public void testScriptStyle() {
    doTest();
  }

  @Test
  public void testSpacesToTabs() {
    doTestWithIndentOptions(4, 4, true);
  }

  @Test
  public void testIndentsInTextBlock() {
    doTest();
  }

  @Test
  public void testComment() {
    doTest();
  }

  @Test
  public void testScript2() {
    doTest();
  }

  @Test
  public void testEmbeddedHtmlPlainText() {
    doTest();
  }

  @Test
  public void testSeveralJavascripts() {
    doTest();
  }

  @Test
  public void testWeb14016() {
    CodeInsightTestFixture myFixture = fixture.get();
    final String fileName = myTestName + ".jade";

    myFixture.configureByFile(fileName);
    doReformat(myFixture.getFile());
    myFixture.checkResultByFile(fileName);
  }

  @Test
  public void testText() {
    doTestWithIndentOptions(4, 4, true);
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    doReformat(myFixture.getFile());
    myFixture.checkResultByFile(myTestName + "_after.jade");
  }

  private void doReformat(final PsiElement file) {
    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(fixture.get().getProject());
    EdtTestUtil.runInEdtAndWait(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      codeStyleManager.reformat(file);
    }));
  }

  private void doTestWithIndentOptions(int indentSize, int tabSize, boolean useTabs) {
    Project project = fixture.get().getProject();
    CodeStyleSettings currSettings = CodeStyle.getSettings(project);
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
    CodeStyleSettingsManager.getInstance(project).setTemporarySettings(tempSettings);
    try {
      doTest();
    }
    finally {
      CodeStyleSettingsManager.getInstance(project).dropTemporarySettings();
    }
  }

  @Test
  public void testUnbufferedSameIndent() {
    doTestWithIndentOptions(4, 4, false);
  }
}
