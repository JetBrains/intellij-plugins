package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartMethodParametersMacro extends Macro {
  @Override
  public String getName() {
    return "dartMethodParameters";
  }

  @Override
  public String getPresentableName() {
    return "dartMethodParameters()";
  }

  @Override
  public Result calculateResult(@NotNull Expression[] params, final ExpressionContext context) {
    final List<String> parameterNames = getContainingMethodParameterNames(context.getPsiElementAtStartOffset());
    if (parameterNames == null) {
      return null;
    }


    List<Result> result = Lists.newArrayList();
    for (String name : parameterNames) {
      result.add(new TextResult(name));
    }

    return new ListResult(result);
  }

  @Override
  public boolean isAcceptableInContext(final TemplateContextType context) {
    return context instanceof DartTemplateContextType;
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

    List<String> results = Lists.newArrayList();

    final List<DartNormalFormalParameter> normalFormalParameters = parameterList.getNormalFormalParameterList();
    for (DartNormalFormalParameter parameter : normalFormalParameters) {
      final DartFunctionSignature signature = parameter.getFunctionSignature();
      if (signature != null) {
        final String name = signature.getName();
        if (name != null) {
          results.add(name);
        }
      }
    }

    final DartNamedFormalParameters namedFormalParameters = parameterList.getNamedFormalParameters();
    if (namedFormalParameters != null) {
      for (DartDefaultFormalNamedParameter parameter : namedFormalParameters.getDefaultFormalNamedParameterList()) {
        final DartComponentName componentName = parameter.getNormalFormalParameter().findComponentName();
        if (componentName != null) {
          final String name = componentName.getName();
          if (name != null) {
            results.add(name);
          }
        }
      }
    }

    return results;
  }
}
