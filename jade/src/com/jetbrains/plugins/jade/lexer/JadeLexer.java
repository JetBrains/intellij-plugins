package com.jetbrains.plugins.jade.lexer;

import com.intellij.lexer.DelegateLexer;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class JadeLexer extends DelegateLexer {

  public JadeLexer(final CodeStyleSettings codeStyleSettings) {
    this(codeStyleSettings, -1);
  }

  public JadeLexer(final CodeStyleSettings codeStyleSettings, int explicitTabSize) {
    super(new JadeEmbeddingLanguagesLexerDecorator(
      new JadeBaseLexer(codeStyleSettings, explicitTabSize)));
  }
}
