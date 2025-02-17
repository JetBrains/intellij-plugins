// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import com.jetbrains.plugins.jade.lexer.JSMetaCodeLexer;
import org.jetbrains.annotations.NotNull;

public class JadeMetaJsLexerTest extends LexerTestCase {
  @Override
  protected @NotNull Lexer createLexer() {
    return new JSMetaCodeLexer();
  }

  @Override
  protected @NotNull String getDirPath() {
    return "plugins/Jade/testData/lexer/metaJs";
  }

  private void defaultTest() {
    doFileTest("jade");
  }

  public void testIfelseMeta() {
    defaultTest();
  }

  public void testIfelseMeta2() {
    defaultTest();
  }

  public void testNestedMeta() {
    defaultTest();
  }

  public void testJsCodeBlock() {
    defaultTest();
  }
}
