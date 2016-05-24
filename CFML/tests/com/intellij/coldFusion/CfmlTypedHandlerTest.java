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
 * Date: 12.01.2009
 */
public class CfmlTypedHandlerTest extends CfmlCodeInsightFixtureTestCase {

  public void testSimpleTagGTCompletion() throws Throwable { doTest('>'); }
  public void testSimpleTagGTCompletion2() throws Throwable { doTest('>'); }

    public void testInnerTagGTCompletion() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTCompletion1() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTCompletion2() throws Throwable {
        doTest('>');
    }

    public void testSimpleTagGTNoCompletion() throws Throwable {
        doTest('>');
    }

    public void testInnerTagGTNoCompletion() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion1() throws Throwable {
        doTest('>');
    }

    public void testOuterTagGTNoCompletion2() throws Throwable {
        doTest('>');
    }

    public void testSeveralTagsGTCompletion() throws Throwable {
        doTest('>');
    }

    public void testInvokeClosingNotInsertion() throws Throwable {
        doTest('>');
    }

    public void testModuleClosingNotInsertion() throws Throwable {
        doTest('>');
    }

    public void testQuoteCompletion() throws Throwable {
        doTest('\"');
    }

    public void testQuoteDeletion() throws Throwable {
        doTest('\b');
    }

    public void testEnterHandler() throws Throwable {
        doTest('\n');
    }

  public void testNpeEnterHandler() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerInsideCfFunction() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerAfterTemplateText() throws Throwable {
    doTest('\n');
  }

  public void testEnterHandlerInsideHtmlBlock() throws Throwable {
    doTest('\n');
  }

  // public void testLiveTemplate() throws Throwable { doTest('\t'); }

  // IDEA-148357, until we calculate properly where we should insert double pounds
  public void testSharpCompletion() throws Throwable {
      doTest('#');
  }

  public void testInnerSharpCompletion() throws Throwable {
      doTest('#');
  }

  public void testRightBracketInQuotes() throws Throwable {
    doTest(')');
  }

  public void testNoInsertionRCurlyBracketIfIncorrect() throws Throwable {
    doTest('\n');
  }

  public void testEditing() throws Throwable {
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

  public void testRightBracketInsertion() throws Throwable { doTest('('); }

  public void testRightSquareBracketInsertion() throws Throwable {
    doTest('[');
  }

  public void testRightCurlyBracketInsertion() throws Throwable {
    doTest('{');
  }

  public void testLeftBracketDeletion() throws Throwable {
    doTest('\b');
  }

  public void testLeftSquareBracketDeletion() throws Throwable {
    doTest('\b');
  }

  public void testLeftCurlyBracketDeletion() throws Throwable {
    doTest('\b');
  }

  private void doTest(final char typed) throws Throwable {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.type(typed);
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
  }

  @Override
  protected String getBasePath() {
      return "/typedHandler";
  }
}
