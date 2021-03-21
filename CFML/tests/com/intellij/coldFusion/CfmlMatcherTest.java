// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.codeInsight.editorActions.CodeBlockUtil;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiDocumentManager;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlMatcherTest extends CfmlCodeInsightFixtureTestCase {
  public static final int BEGIN = 0;
  public static final int END = 1;
  public static final String PAIR_MARKER = "<pair>";

  @Override
  protected String getBasePath() {
    return "/matcher/";
  }

  private void doTest() {
    final int pairOffset = configureByTestFile(getTestName(false));
    int offset = myFixture.getEditor().getCaretModel().getOffset();
    EditorHighlighter editorHighlighter = myFixture.getEditor().getHighlighter();
    HighlighterIterator iterator = editorHighlighter.createIterator(offset);
    boolean forward = offset < pairOffset;
    boolean matched = BraceMatchingUtil
      .matchBrace(myFixture.getEditor().getDocument().getCharsSequence(), myFixture.getFile().getFileType(), iterator, forward);

    assertTrue(matched);
    assertEquals(pairOffset, iterator.getStart());
  }

  private int configureByTestFile(String testName) {
    myFixture.configureByFile(testName + ".cfml");

    String pairMarker = PAIR_MARKER;
    int pairOffset = myFixture.getFile().getText().indexOf(pairMarker);
    if (pairOffset == -1) {
      assertEmpty("File lacks pair marker");
    }

    Document document = myFixture.getEditor().getDocument();
    WriteCommandAction.runWriteCommandAction(getProject(), () -> document.replaceString(pairOffset, pairOffset + pairMarker.length(), ""));
    PsiDocumentManager.getInstance(getProject()).commitDocument(document);

    return pairOffset;
  }

  private void doTestGoingTo(int direction) {
    final int pairOffset = configureByTestFile(getTestName(false));
    if (direction == BEGIN) {
      CodeBlockUtil.moveCaretToCodeBlockStart(getProject(), myFixture.getEditor(), false);
    }
    else {
      CodeBlockUtil.moveCaretToCodeBlockEnd(getProject(), myFixture.getEditor(), false);
    }
    assertEquals(pairOffset, myFixture.getEditor().getCaretModel().getOffset());
  }

  public void testPairedTagsMatching() {
    doTest();
  }

  public void testPairedSeparatedTagsMatching() {
    doTest();
  }

  public void testPairedCurlyBracesMatching() {
    doTest();
  }

  public void testPairedBracesMatchingInExpression() {
    doTest();
  }

  public void testCfmoduleMatching() {
    doTest();
  }

  public void testCfmoduleMatching2() {
    doTest();
  }

  public void testGoToOpeningTag() {
    doTestGoingTo(BEGIN);
  }

  public void testGoToClosingTag() {
    doTestGoingTo(END);
  }

  public void testGoToOpeningCurlyBrace() {
    doTestGoingTo(BEGIN);
  }
}
