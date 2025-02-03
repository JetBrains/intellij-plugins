// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
    String testText1 = loadTestDataFile("1.test.cfml");
    doTest(testText1, loadTestDataFile("1.test.expected"));

    String testText2 = loadTestDataFile("2.test.cfml");
    doTest(testText2, loadTestDataFile("2.test.expected"));
  }

  public void testSqlInjection() {
    doTest();
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

  public void testSqlWithInclude2() throws Throwable {
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
  protected @NotNull Lexer createLexer() {
    return new CfmlLexer(true, null);
  }

  @Override
  protected @NotNull String getDirPath() {
    return "contrib/CFML/testData/lexer";
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

