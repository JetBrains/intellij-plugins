// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Quote;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Angular2BindingImpl extends Angular2EmbeddedExpressionImpl implements Angular2Binding {

  public Angular2BindingImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Binding(this);
    }
    else {
      super.accept(visitor);
    }
  }

  static @Nullable JSExpression getExpression(Angular2EmbeddedExpressionImpl expression) {
    return Arrays.stream(expression.getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS))
      .map(node -> node.getPsi(JSExpression.class))
      .findFirst()
      .orElse(null);
  }

  static Angular2Quote getQuote(Angular2EmbeddedExpressionImpl expression) {
    return Arrays.stream(expression.getChildren(TokenSet.create(Angular2ElementTypes.QUOTE_STATEMENT)))
      .map(node -> node.getPsi(Angular2Quote.class))
      .findFirst()
      .orElse(null);
  }

  @Override
  public @Nullable JSExpression getExpression() {
    return getExpression(this);
  }

  @Override
  public @Nullable Angular2Quote getQuote() {
    return getQuote(this);
  }
}
