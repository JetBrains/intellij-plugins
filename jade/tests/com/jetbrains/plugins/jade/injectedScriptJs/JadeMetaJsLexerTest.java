// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import com.jetbrains.plugins.jade.JadeTestUtil;
import com.jetbrains.plugins.jade.lexer.JSMetaCodeLexer;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

@TestFixtures
public class JadeMetaJsLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(JadeTestUtil.getBaseTestDataPath() + "/lexer/metaJs", () -> new JSMetaCodeLexer());

  private void defaultTest() {
    lexer.get().doFileTest("jade");
  }

  @Test
  public void testIfelseMeta() {
    defaultTest();
  }

  @Test
  public void testIfelseMeta2() {
    defaultTest();
  }

  @Test
  public void testNestedMeta() {
    defaultTest();
  }

  @Test
  public void testJsCodeBlock() {
    defaultTest();
  }
}
