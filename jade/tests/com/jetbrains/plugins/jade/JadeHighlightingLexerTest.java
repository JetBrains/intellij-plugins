// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.lexer.JadeHighlightingLexer;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

@TestApplication
public class JadeHighlightingLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(JadeTestUtil.getBaseTestDataPath() + "lexer", ".txt", true, false, () -> new JadeHighlightingLexer(null));

  @Test
  public void testSimpleHighlighting() {
    defaultTest();
  }

  private void defaultTest() {
    lexer.get().doFileTest("jade");
  }
}
