package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;
import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartClassNameMacro extends Macro {
  @Override
  public String getName() {
    return "dartClassName";
  }

  @Override
  public String getPresentableName() {
    return "dartClassName()";
  }

  @Override
  public Result calculateResult(@NotNull Expression[] params, final ExpressionContext context) {
    final String containingFunctionName = getContainingClassName(context.getPsiElementAtStartOffset());
    return containingFunctionName == null ? null : new TextResult(containingFunctionName);
  }

  @Override
  public boolean isAcceptableInContext(final TemplateContextType context) {
    return context instanceof DartTemplateContextType;
  }

  @VisibleForTesting
  @Nullable
  public String getContainingClassName(@Nullable final PsiElement element) {
    if (element == null) return null;
    final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
    return dartClass == null ? null : dartClass.getName();
  }
}
