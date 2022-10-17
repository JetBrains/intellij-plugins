package com.intellij.plugins.drools.lang.support;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroolsPairedBraceMatcher extends PairedBraceMatcherAdapter {
  public static final BracePair[] PAIRS = new BracePair[]{
      //new BracePair(DroolsTokenTypes.RULE, DroolsTokenTypes.END, true),
      new BracePair(DroolsTokenTypes.LBRACE, DroolsTokenTypes.RBRACE, true),
      new BracePair(DroolsTokenTypes.LBRACKET, DroolsTokenTypes.RBRACKET, true),
      new BracePair(DroolsTokenTypes.LPAREN, DroolsTokenTypes.RPAREN, true),
  };

  public DroolsPairedBraceMatcher() {
    super(new MyPairedBraceMatcher(), DroolsLanguage.INSTANCE);
  }

  private static class MyPairedBraceMatcher implements PairedBraceMatcher {
    @Override
    public BracePair @NotNull [] getPairs() {
      return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType type) {
      return true;
    }

    @Override
    public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
      return openingBraceOffset;
    }
  }
}