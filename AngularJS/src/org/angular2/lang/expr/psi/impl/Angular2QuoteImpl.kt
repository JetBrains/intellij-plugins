// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Quote;
import org.jetbrains.annotations.NotNull;

public class Angular2QuoteImpl extends JSStatementImpl implements Angular2Quote {

  public Angular2QuoteImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public @NotNull String getName() {
    final ASTNode node = findChildByType(JSKeywordSets.IDENTIFIER_NAMES);
    return node != null ? node.getText() : "";
  }

  @Override
  public @NotNull String getContents() {
    final ASTNode colon = findChildByType(JSTokenTypes.COLON);
    return colon != null ? this.getText().substring(colon.getStartOffset() - getStartOffset() + 1) : "";
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Quote(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
