// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DartMethodParametersMacro extends DartMacroBase {

  @Override
  public String getName() {
    return "dartMethodParameters";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, final ExpressionContext context) {
    final List<String> parameterNames = getContainingMethodParameterNames(context.getPsiElementAtStartOffset());
    if (parameterNames == null) {
      return null;
    }


    List<Result> result = new ArrayList<>();
    for (String name : parameterNames) {
      result.add(new TextResult(name));
    }

    return new ListResult(result);
  }

  @Nullable
  @VisibleForTesting
  public List<String> getContainingMethodParameterNames(@Nullable final PsiElement element) {
    if (element == null) return null;

    final DartComponent parent = PsiTreeUtil.getParentOfType(element,
                                                             //DartGetterDeclaration.class, doesn't have parameters
                                                             DartSetterDeclaration.class,
                                                             DartFunctionDeclarationWithBodyOrNative.class,
                                                             DartFactoryConstructorDeclaration.class,
                                                             DartNamedConstructorDeclaration.class,
                                                             DartMethodDeclaration.class,
                                                             DartFunctionDeclarationWithBody.class);

    if (parent == null) return null;

    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(parent, DartFormalParameterList.class);
    if (parameterList == null) return null;

    List<String> results = new ArrayList<>();

    for (DartNormalFormalParameter parameter : parameterList.getNormalFormalParameterList()) {
      findAndAddName(results, parameter);
    }

    final DartOptionalFormalParameters optionalFormalParameters = parameterList.getOptionalFormalParameters();
    if (optionalFormalParameters != null) {
      for (DartDefaultFormalNamedParameter parameter : optionalFormalParameters.getDefaultFormalNamedParameterList()) {
        findAndAddName(results, parameter.getNormalFormalParameter());
      }
    }

    return results;
  }

  private static void findAndAddName(@NotNull final List<String> results, @NotNull final DartNormalFormalParameter parameter) {
    final DartComponentName componentName = parameter.findComponentName();
    if (componentName != null) {
      final String name = componentName.getName();
      if (name != null) {
        results.add(name);
      }
    }
  }
}
