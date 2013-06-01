package com.jetbrains.lang.dart;

import com.intellij.lexer.LookAheadLexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypesSets.WHITE_SPACE;


/**
 * @author: Fedor.Korotkov
 */
public class DartLexer extends LookAheadLexer {

  private static final TokenSet tokensToMerge = TokenSet.create(
    WHITE_SPACE
  );

  public DartLexer() {
    super(new MergingLexerAdapter(new DartFlexLexer(), tokensToMerge));
  }
}
