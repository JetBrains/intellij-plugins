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
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.PsiDocumentManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

  private void doTest() throws Throwable {
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

  private int configureByTestFile(String testName) throws Throwable {
    myFixture.configureByFile(testName + ".cfml");

    String pairMarker = PAIR_MARKER;
    int pairOffset = myFixture.getFile().getText().indexOf(pairMarker);
    if (pairOffset == -1) {
      assertEmpty("File lacks pair marker");
    }

    Document document = myFixture.getEditor().getDocument();
    document.replaceString(pairOffset, pairOffset + pairMarker.length(), "");
    PsiDocumentManager.getInstance(getProject()).commitDocument(document);

    return pairOffset;
  }

  private void doTestGoingTo(int direction) throws Throwable {
    final int pairOffset = configureByTestFile(getTestName(false));
    if (direction == BEGIN) {
      CodeBlockUtil.moveCaretToCodeBlockStart(getProject(), myFixture.getEditor(), false);
    }
    else {
      CodeBlockUtil.moveCaretToCodeBlockEnd(getProject(), myFixture.getEditor(), false);
    }
    assertEquals(pairOffset, myFixture.getEditor().getCaretModel().getOffset());
  }

  public void testPairedTagsMatching() throws Throwable {
    doTest();
  }

  public void testPairedSeparatedTagsMatching() throws Throwable {
    doTest();
  }

  public void testPairedCurlyBracesMatching() throws Throwable {
    doTest();
  }

  public void testPairedBracesMatchingInExpression() throws Throwable {
    doTest();
  }

  public void testCfmoduleMatching() throws Throwable {
    doTest();
  }

  public void testCfmoduleMatching2() throws Throwable {
    doTest();
  }

  public void testGoToOpeningTag() throws Throwable {
    doTestGoingTo(BEGIN);
  }

  public void testGoToClosingTag() throws Throwable {
    doTestGoingTo(END);
  }

  public void testGoToOpeningCurlyBrace() throws Throwable {
    doTestGoingTo(BEGIN);
  }
}
