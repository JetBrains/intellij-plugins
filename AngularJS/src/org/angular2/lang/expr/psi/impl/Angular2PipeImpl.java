// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;
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
  public JSReferenceExpression getNameReference() {
    ASTNode node = getFirstChildNode();
    while (node != null && node.getElementType() != JSTokenTypes.OR) {
      node = node.getTreeNext();
    }
    while (node != null && node.getElementType() != JSElementTypes.REFERENCE_EXPRESSION) {
      node = node.getTreeNext();
    }
    return (JSReferenceExpression)ObjectUtils.doIfNotNull(node, ASTNode::getPsi);
  }

  @Nullable
  @Override
  public String getName() {
    return ObjectUtils.doIfNotNull(getNameReference(), JSReferenceExpression::getReferenceName);
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
