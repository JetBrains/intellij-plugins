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
import com.intellij.testFramework.LexerTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CfmlLexerTest extends LexerTestCase {

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
    Lexer lexer = createLexer();

    String testText1 = loadTestDataFile("1.test.cfml");
    doTest(testText1, loadTestDataFile("1.test.expected"), lexer);

    String testText2 = loadTestDataFile("2.test.cfml");
    doTest(testText2, loadTestDataFile("2.test.expected"), lexer);
  }

  public void testSqlInjection() throws Throwable {
    doTest();
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

  public void testSqlWithInclude() throws Throwable {
    doTest();
  }

  @Override
  protected Lexer createLexer() {
    return new CfmlLexer(true, null);
  }

  @Override
  protected String getDirPath() {
    return "contrib/CFML/tests/testData/lexer";
  }

  @NotNull
  @Override
  protected String getExpectedFileExtension() {
    return ".test.expected";
  }

  private void doTest() throws IOException {
    doFileTest("test.cfml");
  }
}

