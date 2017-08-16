/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

import com.intellij.codeInsight.editorActions.CodeBlockUtil;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiDocumentManager;

/**
 * Created by Lera Nikolaenko
 * Date: 30.10.2008
 */
public class CfmlMatcherTest extends CfmlCodeInsightFixtureTestCase {
  public static final int BEGIN = 0;
  public static final int END = 1;
  public static final String PAIR_MARKER = "<pair>";

  protected String getBasePath() {
    return "/matcher/";
  }

  private void doTest() {
    final int pairOffset = configureByTestFile(getTestName(false));
    int offset = myFixture.getEditor().getCaretModel().getOffset();
    EditorHighlighter editorHighlighter = ((EditorEx)myFixture.getEditor()).getHighlighter();
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
