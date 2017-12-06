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

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.impl.DocumentImpl;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;

import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.getAllTokens;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlTypedHandlerTest extends CfmlCodeInsightFixtureTestCase {

  public void testSimpleTagGTCompletion() { doTest('>'); }
  public void testSimpleTagGTCompletion2() { doTest('>'); }

    public void testInnerTagGTCompletion() {
        doTest('>');
    }

    public void testOuterTagGTCompletion1() {
        doTest('>');
    }

    public void testOuterTagGTCompletion2() {
        doTest('>');
    }

    public void testSimpleTagGTNoCompletion() {
        doTest('>');
    }

    public void testInnerTagGTNoCompletion() {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion1() {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion2() {
        doTest('>');
    }

    public void testSeveralTagsGTCompletion() {
        doTest('>');
    }

    public void testInvokeClosingNotInsertion() {
        doTest('>');
    }

    public void testModuleClosingNotInsertion() {
        doTest('>');
    }

    public void testQuoteCompletion() {
        doTest('\"');
    }

    public void testQuoteDeletion() {
        doTest('\b');
    }

    public void testEnterHandler() {
        doTest('\n');
    }

  public void testNpeEnterHandler() {
    doTest('\n');
  }

  public void testEnterHandlerInsideCfFunction() {
    doTest('\n');
  }

  public void testEnterHandlerAfterTemplateText() {
    doTest('\n');
  }

  public void testEnterHandlerInsideHtmlBlock() {
    doTest('\n');
  }

  // public void testLiveTemplate() throws Throwable { doTest('\t'); }

  // IDEA-148357, until we calculate properly where we should insert double pounds
  public void testSharpCompletion() {
      doTest('#');
  }

  public void testInnerSharpCompletion() {
      doTest('#');
  }

  public void testRightBracketInQuotes() {
    doTest(')');
  }

  public void testNoInsertionRCurlyBracketIfIncorrect() {
    doTest('\n');
  }

  public void testEditing() {
    @NonNls final String s1 = "<table bgcolor=\"#FFFFFF\"><cfoutput>\n" +
                              "  <div id=\"#bColumn2";
    String s2 = "\" />\n" +
                "</cfoutput></table>";
    String s = s1 + s2;

    final Document doc = new DocumentImpl(s);
    EditorEx editor = (EditorEx)EditorFactory.getInstance().createEditor(doc);
    try {
      EditorHighlighter highlighter = HighlighterFactory.createHighlighter(getProject(), CfmlFileType.INSTANCE);
      editor.setHighlighter(highlighter);
      CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(() -> doc.insertString(s1.length(), "#")), "", null);
      List tokensAfterUpdate = getAllTokens(highlighter);
      highlighter = HighlighterFactory.createHighlighter(getProject(), CfmlFileType.INSTANCE);
      editor.setHighlighter(highlighter);
      List tokensWithoutUpdate = getAllTokens(highlighter);
      TestCase.assertEquals(tokensWithoutUpdate, tokensAfterUpdate);
    }
    finally {
      EditorFactory.getInstance().releaseEditor(editor);
    }
  }

  public void testRightBracketInsertion() { doTest('('); }

  public void testRightSquareBracketInsertion() {
    doTest('[');
  }

  public void testRightCurlyBracketInsertion() {
    doTest('{');
  }

  public void testLeftBracketDeletion() {
    doTest('\b');
  }

  public void testLeftSquareBracketDeletion() {
    doTest('\b');
  }

  public void testLeftCurlyBracketDeletion() {
    doTest('\b');
  }

  private void doTest(final char typed) {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.type(typed);
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
  }

  @Override
  protected String getBasePath() {
      return "/typedHandler";
  }
}
