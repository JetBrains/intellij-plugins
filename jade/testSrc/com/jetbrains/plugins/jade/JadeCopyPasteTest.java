// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.idea.IJIgnore;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class JadeCopyPasteTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private int myOriginalReformatOnPaste;
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/copyPaste");
    myOriginalReformatOnPaste = CodeInsightSettings.getInstance().REFORMAT_ON_PASTE;
    CodeInsightSettings.getInstance().REFORMAT_ON_PASTE = CodeInsightSettings.NO_REFORMAT;
  }

  @AfterEach
  void tearDown() {
    CodeInsightSettings.getInstance().REFORMAT_ON_PASTE = myOriginalReformatOnPaste;
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testWholeLine1() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testWholeLine2() {
    doTest();
  }

  @Test
  public void testPartLine1() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testPartLine2() {
    doTest();
  }

  @Test
  public void testPasteInNewlineStart() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testPasteInNewlineIndent() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testPasteInTextOnTheEndOfLine() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testPasteInPipedText() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testLeadingNewLine() {
    doTest();
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testCss() {
    CodeInsightTestFixture myFixture = fixture.get();
    String name = "web_2153";
    myFixture.configureByFile(name + "_source.css");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY);
    myFixture.configureByFile(name + "_target.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    myFixture.checkResultByFile(name + "_after.jade", true);
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = fixture.get();
    String name = myTestName;
    myFixture.configureByFile(name + "_source.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY);
    myFixture.configureByFile(name + "_target.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    myFixture.checkResultByFile(name + "_after.jade", true);
  }
}
