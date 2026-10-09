// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion;

import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestFixtures
public class CfmlLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture("contrib/CFML/testData/lexer", ".test.expected", () -> new CfmlLexer(true, null));

  @Test
  public void testCloseOpenTag() {
    doTest();
  }

  @Test
  public void testAttributes() {
    doTest();
  }

  @Test
  public void testColddocSupportInAttributes() {
    doTest();
  }

  @Test
  public void testTemplateText() {
    doTest();
  }

  @Test
  public void testTagComment() {
    doTest();
  }

  @Test
  public void testCommentBalance() {
    String testText1 = lexer.get().loadTestDataFile("1.test.cfml");
    lexer.get().doTest(testText1, lexer.get().loadTestDataFile("1.test.expected"));

    String testText2 = lexer.get().loadTestDataFile("2.test.cfml");
    lexer.get().doTest(testText2, lexer.get().loadTestDataFile("2.test.expected"));
  }

  @Test
  public void testSqlInjection() {
    doTest();
  }

  @Test
  public void testSharpedAttributeValue() {
    doTest();
  }

  @Test
  public void testSharpsInScript() {
    doTest();
  }

  @Test
  public void testCfTagNamesWithPrefix() {
    doTest();
  }

  @Test
  public void testCfCloseTagWithPrefix() {
    doTest();
  }

  @Test
  public void testVarVariableName() {
    doTest();
  }

  @Test
  public void testVarKeyword() {
    doTest();
  }

  @Test
  public void testSharpInNestedCfOutput() {
    doTest();
  }

  @Test
  public void testSqlWithInclude() {
    doTest();
  }

  @Test
  public void testSqlWithInclude2() {
    doTest();
  }

  @Test
  public void testLexerState() {
    String charSequence = "component name=\"Foo\" {}";
    CfmlLexer cfmlLexer = new CfmlLexer(true, null);
    cfmlLexer.start(charSequence);

    IElementType tokenType = cfmlLexer.getTokenType();
    cfmlLexer.advance();
    cfmlLexer.start(charSequence);
    assertEquals(tokenType, cfmlLexer.getTokenType());
  }

  private void doTest() {
    lexer.get().doFileTest("test.cfml");
  }
}

