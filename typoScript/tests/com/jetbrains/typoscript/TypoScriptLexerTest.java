/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.jetbrains.typoscript;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;
import com.jetbrains.typoscript.lang.TypoScriptLexer;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;


public class TypoScriptLexerTest extends UsefulTestCase {


  public void testSlashComment() throws Throwable {
    doTest();
  }

  public void testSharpComment() throws Throwable {
    doTest();
  }

  public void testSingleLineComments() throws Throwable {
    doTest();
  }

  public void testCommentBlock() throws Throwable {
      doTest();
    }

  public void testSimpleAssignment() throws Throwable {
    doTest();
  }

  public void testAssignments() throws Throwable {
    doTest();
  }

  public void testCopyingOperator() throws Throwable {
    doTest();
  }

  public void testUnsettingOperator() throws Throwable {
    doTest();
  }

  public void testMultilineValue() throws Throwable {
    doTest();
  }

  public void testValueModificationOperator() throws Throwable {
    doTest();
  }

  public void testInclude() throws Throwable {
    doTest();
  }

  public void testObjectPaths() throws Throwable {
    doTest();
  }

  public void testCodeBlock() throws Throwable {
    doTest();
  }

  public void testExampleFromTypo() throws Throwable {
    doTest();
  }

  public void testComments() throws Throwable {
    doTest();
  }

  public void testCondition() throws Throwable {
    doTest();
  }

  private void doTest() throws IOException {
    Lexer lexer = new TypoScriptLexer();
    String testText = TypoScriptTestUtil.getInputData(getDataSubPath(), getTestName(true));
    String expected = TypoScriptTestUtil.getExpectedDataFilePath(getDataSubPath(), getTestName(true));
    doFileLexerTest(lexer, testText, expected);
  }

  private static void doFileLexerTest(Lexer lexer, String testText, String expected) {
    lexer.start(testText);
    String result = "";
    for (IElementType tokenType = lexer.getTokenType(); tokenType != null; tokenType = lexer.getTokenType()) {
      String tokenText = lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
      String tokenTypeName = tokenType.toString();
      String line = tokenTypeName + " ('" + tokenText + "')\n";
      result += line;
      lexer.advance();
    }
    UsefulTestCase.assertSameLinesWithFile(expected, result);
  }

  @NonNls
  private static String getDataSubPath() {
    return TypoScriptTestUtil.getDataSubPath("lexer");
  }
}