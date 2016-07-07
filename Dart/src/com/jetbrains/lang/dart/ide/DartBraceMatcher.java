package com.jetbrains.lang.dart.ide;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] BRACE_PAIRS = {
    new BracePair(DartTokenTypes.LBRACE, DartTokenTypes.RBRACE, true),
    new BracePair(DartTokenTypes.LBRACKET, DartTokenTypes.RBRACKET, false),
    new BracePair(DartTokenTypes.LPAREN, DartTokenTypes.RPAREN, false),
    new BracePair(DartTokenTypes.LONG_TEMPLATE_ENTRY_START, DartTokenTypes.LONG_TEMPLATE_ENTRY_END, false)
  };

  @Override
  public BracePair[] getPairs() {
    return BRACE_PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return contextType == null || !DartTokenTypesSets.STRINGS.contains(contextType);
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
