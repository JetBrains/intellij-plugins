package com.jetbrains.lang.dart.ide;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartBraceMatcher implements PairedBraceMatcher {
  private static BracePair[] ourBracePairs =
    {
      new BracePair(DartTokenTypes.LBRACE, DartTokenTypes.RBRACE, true),
      new BracePair(DartTokenTypes.LBRACKET, DartTokenTypes.RBRACKET, false),
      new BracePair(DartTokenTypes.LPAREN, DartTokenTypes.RPAREN, false)
    };

  @Override
  public BracePair[] getPairs() {
    return ourBracePairs;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
