// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.testFramework.LexerTestCase;
import com.jetbrains.plugins.jade.lexer.JadeHighlightingLexer;

import java.io.File;
import java.io.IOException;

public class JadeHighlightingLexerTest extends JadeBaseParsingTestCase {

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "lexer";
  }


  public void testSimpleHighlighting() {
    defaultTest();
  }

  private void defaultTest() {
    String name = getTestName(true);

    try {
      String text = loadFile(name + "." + myFileExt);
      final JadeHighlightingLexer lexer = new JadeHighlightingLexer(null);

      final String lexedText = LexerTestCase.printTokens(text, 0, lexer);

      assertSameLinesWithFile(myFullDataPath + File.separator + name + ".txt", lexedText);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
