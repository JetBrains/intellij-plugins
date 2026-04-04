// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.idea.IJIgnore;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeCopyPasteTest extends BasePlatformTestCase {

  private int myOriginalReformatOnPaste;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myOriginalReformatOnPaste = CodeInsightSettings.getInstance().REFORMAT_ON_PASTE;
    CodeInsightSettings.getInstance().REFORMAT_ON_PASTE = CodeInsightSettings.NO_REFORMAT;
  }

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/copyPaste";
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      CodeInsightSettings.getInstance().REFORMAT_ON_PASTE = myOriginalReformatOnPaste;
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @IJIgnore(issue = "AT-4013")
  public void testWholeLine1() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testWholeLine2() {
    doTest();
  }

  public void testPartLine1() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testPartLine2() {
    doTest();
  }

  public void testPasteInNewlineStart() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testPasteInNewlineIndent() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testPasteInTextOnTheEndOfLine() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testPasteInPipedText() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testLeadingNewLine() {
    doTest();
  }

  @IJIgnore(issue = "AT-4013")
  public void testCss() {
    String name = "web_2153";
    myFixture.configureByFile(name + "_source.css");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY);
    myFixture.configureByFile(name + "_target.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    myFixture.checkResultByFile(name + "_after.jade", true);
  }

  private void doTest() {
    String name = getTestName(true);
    myFixture.configureByFile(name + "_source.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY);
    myFixture.configureByFile(name + "_target.jade");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    myFixture.checkResultByFile(name + "_after.jade", true);
  }
}
