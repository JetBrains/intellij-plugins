// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.types.JSLazyExpressionType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.lang.expr.parser.Angular2ElementTypes.PIPE_ARGUMENTS_LIST;

public class Angular2PipeExpressionImpl extends JSExpressionImpl implements Angular2PipeExpression, JSCallLikeExpressionCommon {

  public Angular2PipeExpressionImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2PipeExpression(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Nullable
  @Override
  public JSElementIndexingData getIndexingData() {
    return null;
  }

  @Nullable
  @Override
  public String getName() {
    return ObjectUtils.doIfNotNull(getNameReference(), JSReferenceExpression::getReferenceName);
  }

  @NotNull
  @Override
  public List<JSType> getArgumentTypes(boolean contextual) {
    return JSLazyExpressionType.mapAsArguments(getArguments(), contextual);
  }

  @Override
  public JSExpression getMethodExpression() {
    return getNameReference();
  }

  @Override
  public JSExpression getStubSafeMethodExpression() {
    return null;
  }

  @NotNull
  @Override
  public JSArgumentList getArgumentList() {
    final ASTNode node = findChildByType(PIPE_ARGUMENTS_LIST);
    assert node != null;
    return node.getPsi(JSArgumentList.class);
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    return getArgumentList().getArguments();
  }

  @Override
  public boolean isRequireCall() {
    return false;
  }

  @Override
  public boolean isDefineCall() {
    return false;
  }

  @Nullable
  private JSReferenceExpression getNameReference() {
    return (JSReferenceExpression)findPsiChildByType(Angular2ElementTypes.PIPE_REFERENCE_EXPRESSION);
  }
}
