package com.jetbrains.lang.dart.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT_BODY;

public class DartDocLexer extends MergingLexerAdapter {

  public DartDocLexer() {
    super(new FlexAdapter(new _DartDocLexer()), TokenSet.create(MULTI_LINE_COMMENT_BODY));
  }
}
