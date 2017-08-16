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
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LexerTestCase;
import org.jetbrains.annotations.NotNull;

public class CfmlLexerTest extends LexerTestCase {

  public void testCloseOpenTag() {
    doTest();
  }

  public void testAttributes() {
    doTest();
  }

  public void testColddocSupportInAttributes() {
    doTest();
  }

  public void testTemplateText() {
    doTest();
  }

  public void testTagComment() {
    doTest();
  }

  public void testCommentBalance() {
    Lexer lexer = createLexer();

    String testText1 = loadTestDataFile("1.test.cfml");
    doTest(testText1, loadTestDataFile("1.test.expected"), lexer);

    String testText2 = loadTestDataFile("2.test.cfml");
    doTest(testText2, loadTestDataFile("2.test.expected"), lexer);
  }

  public void testSharpedAttributeValue() {
    doTest();
  }

  public void testSharpsInScript() {
    doTest();
  }

  public void testCfTagNamesWithPrefix() {
    doTest();
  }

  public void testCfCloseTagWithPrefix() {
    doTest();
  }

  public void testVarVariableName() {
    doTest();
  }

  public void testVarKeyword() {
    doTest();
  }

  public void testSharpInNestedCfOutput() {
    doTest();
  }

  public void testSqlWithInclude() {
    doTest();
  }

  public void testLexerState() {
    String charSequence = "component name=\"Foo\" {}";
    CfmlLexer lexer = new CfmlLexer(true, null);
    lexer.start(charSequence);
    
    IElementType tokenType = lexer.getTokenType();
    lexer.advance();
    lexer.start(charSequence);
    assertEquals(tokenType, lexer.getTokenType());
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

  private void doTest() {
    doFileTest("test.cfml");
  }
}

