// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Pipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.expr.parser.Angular2ElementTypes.ARGUMENT_LIST;

public class Angular2PipeImpl extends JSExpressionImpl implements Angular2Pipe {

  public Angular2PipeImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Pipe(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Nullable
  @Override
  public JSExpression getExpression() {
    final ASTNode node = findChildByType(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS);
    return node != null ? node.getPsi(JSExpression.class) : null;
  }

  @Nullable
  @Override
  public String getName() {
    final ASTNode node = findChildByType(JSKeywordSets.IDENTIFIER_NAMES);
    return node != null ? node.getText() : null;
  }

  @Nullable
  @Override
  public JSArgumentList getArgumentList() {
    final ASTNode node = findChildByType(ARGUMENT_LIST);
    return node != null ? node.getPsi(JSArgumentList.class) : null;
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    JSArgumentList args = getArgumentList();
    return args != null ? args.getArguments() : JSExpression.EMPTY_ARRAY;
  }
}
