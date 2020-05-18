// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angular2.lang.expr.psi.Angular2PipeLeftSideArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.lang.expr.parser.Angular2ElementTypes.PIPE_LEFT_SIDE_ARGUMENT;

public class Angular2PipeExpressionImpl extends JSExpressionImpl implements Angular2PipeExpression, JSCallLikeExpressionCommon {

  public Angular2PipeExpressionImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2PipeExpression(this);
    }
    else if (visitor instanceof JSElementVisitor) {
      ((JSElementVisitor)visitor).visitJSCallExpression(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public @Nullable JSElementIndexingData getIndexingData() {
    return null;
  }

  @Override
  public @Nullable String getName() {
    return ObjectUtils.doIfNotNull(getNameReference(), JSReferenceExpression::getReferenceName);
  }

  @Override
  public @NotNull List<JSType> getArgumentTypes(boolean contextual) {
    return JSPsiBasedTypeOfType.mapAsArguments(getArguments(), contextual);
  }

  @Override
  public JSExpression getMethodExpression() {
    return getNameReference();
  }

  @Override
  public JSExpression getStubSafeMethodExpression() {
    return null;
  }

  @Override
  public @NotNull JSArgumentList getArgumentList() {
    return getLeftSideArgument();
  }

  @NotNull
  Angular2PipeLeftSideArgument getLeftSideArgument() {
    final ASTNode node = findChildByType(PIPE_LEFT_SIDE_ARGUMENT);
    assert node != null;
    return node.getPsi(Angular2PipeLeftSideArgument.class);
  }

  @Override
  public boolean isRequireCall() {
    return false;
  }

  @Override
  public boolean isDefineCall() {
    return false;
  }

  @Override
  public boolean isElvis() {
    return false;
  }

  private @Nullable JSReferenceExpression getNameReference() {
    return (JSReferenceExpression)findPsiChildByType(Angular2ElementTypes.PIPE_REFERENCE_EXPRESSION);
  }
}
