package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.codeInsight.template.macro.MethodNameMacro;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBody;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartMethodNameMacro extends MethodNameMacro {

  @Override
  public Result calculateResult(@NotNull Expression[] params, final ExpressionContext context) {
    final String containingFunctionName = getContainingFunctionName(context.getPsiElementAtStartOffset());
    return containingFunctionName == null ? null : new TextResult(containingFunctionName);
  }

  @VisibleForTesting
  @Nullable
  public String getContainingFunctionName(@Nullable final PsiElement element) {
    if (element == null) return null;
    final DartComponent parent = (DartComponent)PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
      @Override
      public boolean value(final PsiElement element) {
        return element instanceof DartFunctionDeclarationWithBodyOrNative || element instanceof DartFunctionDeclarationWithBody ||
               element instanceof DartMethodDeclaration;
      }
    });
    if (parent == null) return null;
    final DartComponentName componentName = parent.getComponentName();
    return componentName == null ? null : componentName.getName();
  }

  @Override
  public boolean isAcceptableInContext(final TemplateContextType context) {
    return context instanceof DartTemplateContextType;
  }
}

