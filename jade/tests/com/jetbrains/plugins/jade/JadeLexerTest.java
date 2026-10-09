// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lexer.Lexer;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLexer;
import com.jetbrains.plugins.jade.lexer.JadeLexer;
import com.jetbrains.plugins.jade.lexer.JadeSimpleInterpolationLexer;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

// The application is needed for various XML extension points registration
@TestApplication
public class JadeLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(JadeTestUtil.getBaseTestDataPath() + "/lexer", ".txt", false, () -> new JadeLexer(null, 2));

  @Test
  public void testSimple() {
    defaultTest();
  }

  @Test
  public void testText1() {
    defaultTest();
  }

  @Test
  public void testAttributes() {
    defaultTest();
  }

  @Test
  public void testAttributes2() {
    defaultTest();
  }

  @Test
  public void testEs6StringAttributes() {
    defaultTest();
  }

  @Test
  public void testInterp1() {
    defaultTest();
  }

  @Test
  public void testInterp2() {
    defaultTest();
  }

  @Test
  public void testInterp3() {
    defaultTest();
  }

  @Test
  public void testBlocks() {
    defaultTest();
  }

  @Test
  public void testScriptStyle1() {
    defaultTest();
  }

  @Test
  public void testScript() {
    defaultTest();
  }

  @Test
  public void testScriptEs6() {
    defaultTest();
  }

  @Test
  public void testScriptStyleOneliner() {
    defaultTest();
  }

  @Test
  public void testConditionals() {
    defaultTest();
  }

  @Test
  public void testFilters() {
    defaultTest();
  }

  @Test
  public void testBufferedOutput() {
    defaultTest();
  }

  @Test
  public void testWhitespaceBeforeBlock() {
    defaultTest();
  }

  private void defaultTest() {
    lexer.get().doFileTest("jade");
  }

  @Test
  public void testScriptWithDot() {
    defaultTest();
  }

  @Test
  public void testCase() {
    defaultTest();
  }

  @Test
  public void testPlainExpressionLine() {
    defaultTest();
  }

  @Test
  public void testEmbeddedHtmlPlainText() {
    defaultTest();
  }

  @Test
  public void testVariousScripts() {
    defaultTest();
  }

  @Test
  public void testExtendedKeywords() {
    defaultTest();
  }

  @Test
  public void testMixins() {
    defaultTest();
  }

  @Test
  public void testIfelseJade() {
    defaultTest();
  }

  @Test
  public void testManyOnelinersJade() {
    defaultTest();
  }

  @Test
  public void testWeb12957() {
    defaultTest();
  }

  @Test
  public void testEa59204() {
    defaultTest();
  }

  @Test
  public void testEa59518() {
    defaultTest();
  }

  @Test
  public void testAngular2() {
    defaultTest();
  }

  @Test
  public void testEscapedNewline() {
    defaultTest();
  }

  @Test
  public void testAttributeWithConditional() {
    defaultTest();
  }

  @Test
  public void testInterpStress() {
    final Lexer lexer = new JadeSimpleInterpolationLexer(new JavaScriptInJadeLexer());
    final String textToLex = "var abc = 'abc#{trava}cba'";
    for (int i = 0; i < 16000; ++i) {
      lexer.start(textToLex);
      while (lexer.getTokenType() != null) {
        lexer.advance();
      }
    }
  }
}
