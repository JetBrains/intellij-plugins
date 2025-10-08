// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLexer;
import com.jetbrains.plugins.jade.lexer.JadeLexer;
import com.jetbrains.plugins.jade.lexer.JadeSimpleInterpolationLexer;
import org.jetbrains.annotations.NotNull;

public class JadeLexerTest extends LexerTestCase {
  private IdeaProjectTestFixture myFixture;

  @Override
  protected @NotNull Lexer createLexer() {
    return new JadeLexer(null, 2);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // needed for various XML extension points registration
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).getFixture();
    myFixture.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected @NotNull String getDirPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/lexer";
  }

  @Override
  protected @NotNull String getPathToTestDataFile(@NotNull String extension) {
    return getDirPath() + "/" + getTestName(true) + extension;
  }

  public void testSimple() {
    defaultTest();
  }

  public void testText1() {
    defaultTest();
  }

  public void testAttributes() {
    defaultTest();
  }

  public void testAttributes2() {
    defaultTest();
  }

  public void testEs6StringAttributes() {
    defaultTest();
  }

  public void testInterp1() {
    defaultTest();
  }

  public void testInterp2() {
    defaultTest();
  }

  public void testInterp3() {
    defaultTest();
  }

  public void testBlocks() {
    defaultTest();
  }

  public void testScriptStyle1() {
    defaultTest();
  }

  public void testScript() {
    defaultTest();
  }

  public void testScriptEs6() {
    defaultTest();
  }

  public void testScriptStyleOneliner() {
    defaultTest();
  }

  public void testConditionals() {
    defaultTest();
  }

  public void testFilters() {
    defaultTest();
  }

  public void testBufferedOutput() {
    defaultTest();
  }

  public void testWhitespaceBeforeBlock() {
    defaultTest();
  }

  private void defaultTest() {
    doFileTest("jade");
  }

  public void testScriptWithDot() {
    defaultTest();
  }

  public void testCase() {
    defaultTest();
  }

  public void testPlainExpressionLine() {
    defaultTest();
  }

  public void testEmbeddedHtmlPlainText() {
    defaultTest();
  }

  public void testVariousScripts() {
    defaultTest();
  }

  public void testExtendedKeywords() {
    defaultTest();
  }

  public void testMixins() {
    defaultTest();
  }

  public void testIfelseJade() {
    defaultTest();
  }

  public void testManyOnelinersJade() {
    defaultTest();
  }

  public void testWeb12957() {
    defaultTest();
  }

  public void testEa59204() {
    defaultTest();
  }

  public void testEa59518() {
    defaultTest();
  }

  public void testAngular2() {
    defaultTest();
  }

  public void testEscapedNewline() {
    defaultTest();
  }

  public void testAttributeWithConditional() {
    defaultTest();
  }

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

  @Override
  protected boolean shouldTrim() {
    return false;
  }
}
