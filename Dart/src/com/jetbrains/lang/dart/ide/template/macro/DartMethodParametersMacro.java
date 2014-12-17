package com.jetbrains.lang.dart.ide.template.macro;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.ListResult;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.codeInsight.template.macro.MethodParametersMacro;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartDefaultFormalNamedParameter;
import com.jetbrains.lang.dart.psi.DartFormalParameterList;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBody;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartFunctionSignature;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import com.jetbrains.lang.dart.psi.DartNamedFormalParameters;
import com.jetbrains.lang.dart.psi.DartNormalFormalParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartMethodParametersMacro extends MethodParametersMacro {

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

    final DartComponent parent = (DartComponent)PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
      @Override
      public boolean value(final PsiElement element) {
        return element instanceof DartFunctionDeclarationWithBodyOrNative || element instanceof DartFunctionDeclarationWithBody ||
               element instanceof DartMethodDeclaration;
      }
    });
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
