package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFilterExpression extends JSExpressionImpl implements JSCallExpression, JSCallLikeExpressionCommon {
  public AngularJSFilterExpression(IElementType elementType) {
    super(elementType);
  }

  public static boolean isFilterNameRef(PsiReference ref, PsiElement parent) {
    return parent instanceof AngularJSFilterExpression && ((AngularJSFilterExpression)parent).getFilterName() == ref;
  }

  public JSReferenceExpression getFilterName() {
    return (JSReferenceExpression)getFirstChild();
  }

  @Override
  public JSExpression getMethodExpression() {
    final ASTNode node = JSCallExpressionImpl.getMethodExpression(getNode());
    return node != null ? (JSExpression)node.getPsi() : null;
  }

  @Override
  public JSExpression getStubSafeMethodExpression() {
    return null;
  }

  @Override
  public @Nullable JSArgumentList getArgumentList() {
    final ASTNode argumentList = JSCallExpressionImpl.getArgumentList(getNode());
    return argumentList != null ? (JSArgumentList)argumentList.getPsi() : null;
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
    return JSCallExpression.super.isElvis();
  }

  @Override
  public @Nullable JSElementIndexingData getIndexingData() {
    return null;
  }
}
