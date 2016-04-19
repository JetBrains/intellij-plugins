package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public abstract class DartStringLiteralExpressionBase extends DartClassReferenceImpl implements DartStringLiteralExpression {

  public DartStringLiteralExpressionBase(ASTNode node) {
    super(node);
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull final String text) {
    // TODO
    return this;
  }

  @NotNull
  @Override
  public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    // TODO
    return LiteralTextEscaper.createSimple(this);
  }
}
