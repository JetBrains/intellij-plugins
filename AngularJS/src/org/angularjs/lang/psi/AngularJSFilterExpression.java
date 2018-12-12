package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.types.JSLazyExpressionType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

  @NotNull
  @Override
  public List<JSType> getArgumentTypes(boolean contextual) {
    return JSLazyExpressionType.mapAsArguments(getArguments(), contextual);
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

  @Nullable
  @Override
  public JSArgumentList getArgumentList() {
    final ASTNode argumentList = JSCallExpressionImpl.getArgumentList(getNode());
    return argumentList != null ? (JSArgumentList)argumentList.getPsi() : null;
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    JSArgumentList argumentList = getArgumentList();
    return argumentList != null ? argumentList.getArguments() : JSExpression.EMPTY_ARRAY;
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
  @Override
  public JSElementIndexingData getIndexingData() {
    return null;
  }
}
