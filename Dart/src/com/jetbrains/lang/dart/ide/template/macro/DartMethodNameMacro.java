package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartMethodNameMacro extends DartMacroBase {
  @Override
  public String getName() {
    return "dartMethodName";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, final ExpressionContext context) {
    final String containingFunctionName = getContainingFunctionName(context.getPsiElementAtStartOffset());
    return containingFunctionName == null ? null : new TextResult(containingFunctionName);
  }

  @VisibleForTesting
  @Nullable
  public String getContainingFunctionName(@Nullable final PsiElement element) {
    if (element == null) return null;
    final DartComponent parent = PsiTreeUtil.getParentOfType(element,
                                                             DartGetterDeclaration.class,
                                                             DartSetterDeclaration.class,
                                                             DartFunctionDeclarationWithBodyOrNative.class,
                                                             DartFactoryConstructorDeclaration.class,
                                                             DartNamedConstructorDeclaration.class,
                                                             DartMethodDeclaration.class,
                                                             DartFunctionDeclarationWithBody.class);
    if (parent == null) return null;
    final DartComponentName componentName = parent.getComponentName();
    return componentName == null ? null : componentName.getName();
  }
}

