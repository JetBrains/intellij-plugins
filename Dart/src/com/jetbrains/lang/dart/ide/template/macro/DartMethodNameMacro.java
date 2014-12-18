package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartMethodNameMacro extends Macro {
  @Override
  public String getName() {
    return "dartMethodName";
  }

  @Override
  public String getPresentableName() {
    return "dartMethodName()";
  }

  @Override
  public Result calculateResult(@NotNull Expression[] params, final ExpressionContext context) {
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

  @Override
  public boolean isAcceptableInContext(final TemplateContextType context) {
    return context instanceof DartTemplateContextType;
  }
}

