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

import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;

import java.io.File;
import java.io.IOException;

public class CfmlLexerTest extends UsefulTestCase {

  public void testCloseOpenTag() throws Throwable {
    doTest();
  }

  public void testAttributes() throws Throwable {
    doTest();
  }

  public void testColddocSupportInAttributes() throws Throwable {
    doTest();
  }

  public void testTemplateText() throws Throwable {
    doTest();
  }

  public void testTagComment() throws Throwable {
    doTest();
  }

  public void testCommentBalance() throws Throwable {
    Lexer lexer = new CfmlLexer(true, null);
    String testText1 = loadFile(getTestName(true) + "1.test.cfml");
    String expected1 = getDataSubpath() + getTestName(true) + "1.test.expected";
    doFileLexerTest(lexer, testText1, expected1);

    String testText2 = loadFile(getTestName(true) + "2.test.cfml");
    String expected2 = getDataSubpath() + getTestName(true) + "2.test.expected";
    doFileLexerTest(lexer, testText2, expected2);
  }

  public void testSharpedAttributeValue() throws Throwable {
    doTest();
  }

  public void testSharpsInScript() throws Throwable {
    doTest();
  }

  public void testCfTagNamesWithPrefix() throws Throwable {
    doTest();
  }

  public void testCfCloseTagWithPrefix() throws Throwable {
    doTest();
  }

  public void testVarVariableName() throws Throwable {
    doTest();
  }

  public void testVarKeyword() throws Throwable {
    doTest();
  }

  public void testSharpInNestedCfOutput() throws Throwable {
    doTest();
  }

  private static void doFileLexerTest(Lexer lexer, String testText, String expectedFilePath) {
    lexer.start(testText);
    String result = "";
    for (; ; ) {
      IElementType tokenType = lexer.getTokenType();
      if (tokenType == null) {
        break;
      }
      String tokenText = getTokenText(lexer);
      String tokenTypeName = tokenType.toString();
      String line = tokenTypeName + " ('" + tokenText + "')\n";
      result += line;
      lexer.advance();
    }
    assertSameLinesWithFile(expectedFilePath, result);
  }

  private void doTest() throws IOException {
    Lexer lexer = new CfmlLexer(true, null);
    String testText = loadFile(getTestName(true) + ".test.cfml");
    doFileLexerTest(lexer, testText, getDataSubpath() + getTestName(true) + ".test.expected");
  }

  private String loadFile(String path) throws IOException {
    return FileUtil.loadFile(new File(FileUtil.toSystemDependentName(getDataSubpath() + path)));
  }

  private static String getTokenText(Lexer lexer) {
    return lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
  }

  protected String getDataSubpath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/lexer/";
  }
}

