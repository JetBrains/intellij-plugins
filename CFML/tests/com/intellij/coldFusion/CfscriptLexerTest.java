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

/**
 * Created by Lera Nikolaenko
 * Date: 21.01.2009
 */
public class CfscriptLexerTest extends UsefulTestCase {

  public void testSimpleScript() throws Throwable {
    doTest();
  }

  public void testVarVariableName() throws Throwable {
    doTest();
  }

  public void testOneLineComment() throws Throwable {
    doTest();
  }

  public void testMultiLineComment() throws Throwable {
    doTest();
  }

  public void testDefineComponentWithoutTag() throws Throwable {
    doTest();
  }

  public void testAllowImportsBeforeComponentDecl() throws Throwable {
    doTest();
  }

  public void testMultiplePoundSignsInStringLiteral() throws Throwable {
    doTest();
  }


  private void doFileLexerTest(Lexer lexer, String testText, String expectedFileName) {
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
    assertSameLinesWithFile(expectedFileName, result);
  }

  private void doTest() throws IOException {
    Lexer lexer = new CfmlLexer(true, null);
    String testText = loadFile(getTestName(true) + ".test.cfml");
    doFileLexerTest(lexer, testText, getDataSubpath() + getTestName(true) + ".test.expected");
  }

  private String loadFile(String fileName) throws IOException {
    return FileUtil.loadFile(new File(FileUtil.toSystemDependentName(getDataSubpath() + fileName)));
  }

  private static String getTokenText(Lexer lexer) {
    return lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
  }

  protected String getDataSubpath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/cfscript/lexer/";
  }
}
