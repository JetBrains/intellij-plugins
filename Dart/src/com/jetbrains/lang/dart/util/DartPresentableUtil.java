// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DartPresentableUtil {

  public static final @NonNls String RIGHT_ARROW = UIUtil.rightArrow();
  private static final @NonNls String SPACE = " ";

  public static String setterGetterName(String name) {
    return name.startsWith("_") ? name.substring(1) : name;
  }

  public static @NotNull String getPresentableParameterList(DartComponent element) {
    return getPresentableParameterList(element, new DartGenericSpecialization());
  }

  public static @NotNull String getPresentableParameterList(DartComponent element, DartGenericSpecialization specialization) {
    return getPresentableParameterList(element, specialization, false, false, false);
  }

  public static @NotNull @NlsSafe String getPresentableParameterList(DartComponent element,
                                                                     DartGenericSpecialization specialization,
                                                                     boolean functionalStyleSignatures,
                                                                     boolean displayDefaultValues,
                                                                     boolean displayFinalKeyword) {
    final StringBuilder result = new StringBuilder();
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    if (parameterList == null) {
      return "";
    }
    final List<DartNormalFormalParameter> list = parameterList.getNormalFormalParameterList();
    for (int i = 0, size = list.size(); i < size; i++) {
      result.append(getPresentableNormalFormalParameter(list.get(i), specialization, functionalStyleSignatures, displayDefaultValues,
                                                        displayFinalKeyword));
      if (i < size - 1) {
        result.append(", ");
      }
    }
    final DartOptionalFormalParameters optionalFormalParameters = parameterList.getOptionalFormalParameters();
    if (optionalFormalParameters != null) {
      if (!list.isEmpty()) {
        result.append(", ");
      }

      final boolean isOptional = isOptionalParameterList(optionalFormalParameters);
      result.append(isOptional ? '{' : '[');

      List<DartDefaultFormalNamedParameter> list1 = optionalFormalParameters.getDefaultFormalNamedParameterList();
      for (int i = 0, size = list1.size(); i < size; i++) {
        if (i > 0) {
          result.append(", ");
        }
        DartDefaultFormalNamedParameter formalParameter = list1.get(i);
        result.append(
          getPresentableNormalFormalParameter(formalParameter.getNormalFormalParameter(), specialization, functionalStyleSignatures,
                                              displayDefaultValues, displayFinalKeyword));
      }
      result.append(isOptional ? '}' : ']');
    }
    return result.toString();
  }

  private static boolean isOptionalParameterList(final @NotNull DartOptionalFormalParameters parameters) {
    // Workaround for the lack of distinction between named and optional params in the grammar
    final PsiElement firstChild = parameters.getFirstChild();
    return firstChild != null && "{".equals(firstChild.getText());
  }

  public static String getPresentableNormalFormalParameter(DartNormalFormalParameter parameter, DartGenericSpecialization specialization) {
    return getPresentableNormalFormalParameter(parameter, specialization, false, false, false);
  }

  public static String getPresentableNormalFormalParameter(DartNormalFormalParameter parameter,
                                                           DartGenericSpecialization specialization,
                                                           final boolean functionalStyleSignature,
                                                           final boolean displayDefaultValues,
                                                           final boolean displayFinalKeyword) {
    final StringBuilder result = new StringBuilder();

    final DartFunctionFormalParameter functionFormalParameter = parameter.getFunctionFormalParameter();
    final DartFieldFormalParameter fieldFormalParameter = parameter.getFieldFormalParameter();
    final DartSimpleFormalParameter simpleFormalParameter = parameter.getSimpleFormalParameter();

    if (functionFormalParameter != null) {
      final DartReturnType returnType = functionFormalParameter.getReturnType();
      if (!functionalStyleSignature && returnType != null) {
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), returnType, specialization));
        result.append(SPACE);
      }

      result.append(functionFormalParameter.getName());
      result.append("(");
      result.append(getPresentableParameterList(functionFormalParameter, specialization, functionalStyleSignature, displayDefaultValues,
                                                displayFinalKeyword));
      result.append(")");

      if (functionalStyleSignature && returnType != null) {
        result.append(SPACE);
        result.append(RIGHT_ARROW);
        result.append(SPACE);
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), returnType, specialization));
      }
    }
    else if (fieldFormalParameter != null) {
      DartType type = fieldFormalParameter.getType();

      if (type == null) {
        final PsiElement resolve = fieldFormalParameter.getReferenceExpression().resolve();
        DartVarDeclarationList varDeclarationList = PsiTreeUtil.getParentOfType(resolve, DartVarDeclarationList.class);
        if (varDeclarationList != null) {
          type = varDeclarationList.getVarAccessDeclaration().getType();
        }
      }

      if (type != null) {
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), type, specialization));
        result.append(SPACE);
      }

      result.append(fieldFormalParameter.getReferenceExpression().getText());
    }
    else if (simpleFormalParameter != null) {
      if (displayDefaultValues) {
        final PsiElement defaultFormalNamedParameter =
          PsiTreeUtil.getParentOfType(simpleFormalParameter, DartDefaultFormalNamedParameter.class);
        if (defaultFormalNamedParameter != null) {
          result.append(defaultFormalNamedParameter.getText());
        }
        else {
          if (displayFinalKeyword && simpleFormalParameter.isFinal()) {
            result.append(DartTokenTypes.FINAL.toString());
            result.append(SPACE);
          }
          final DartType type = simpleFormalParameter.getType();
          if (type != null) {
            result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), type, specialization));
            result.append(SPACE);
          }
          result.append(simpleFormalParameter.getComponentName().getText());
        }
      }
      else {
        if (displayFinalKeyword && simpleFormalParameter.isFinal()) {
          result.append(DartTokenTypes.FINAL.toString());
          result.append(SPACE);
        }
        final DartType type = simpleFormalParameter.getType();
        if (type != null) {
          result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), type, specialization));
          result.append(SPACE);
        }
        result.append(simpleFormalParameter.getComponentName().getText());
      }
    }

    return result.toString();
  }

  public static @NotNull @NlsSafe String buildTypeText(@Nullable DartComponent element,
                                                       @NotNull DartReturnType returnType,
                                                       @Nullable DartGenericSpecialization specializations) {
    return returnType.getType() == null ? "void" : buildTypeText(element, returnType.getType(), specializations);
  }

  public static @NotNull @NlsSafe String buildTypeText(@Nullable DartComponent element,
                                                       @Nullable DartType type,
                                                       @Nullable DartGenericSpecialization specializations) {
    if (type == null) {
      return "";
    }
    final StringBuilder result = new StringBuilder();
    final String typeText;
    final DartReferenceExpression expression = type.getReferenceExpression();
    if (expression != null) {
      typeText = expression.getText();

      if (specializations != null && !typeText.isEmpty() && specializations.containsKey(element, typeText)) {
        final DartClass dartClass = specializations.get(element, typeText).getDartClass();
        result.append(dartClass == null ? typeText : dartClass.getName());
      }
      else {
        result.append(typeText);
      }
      final DartTypeArguments typeArguments = type.getTypeArguments();
      if (typeArguments != null) {
        result.append("<");
        List<DartType> list = typeArguments.getTypeList().getTypeList();
        for (int i = 0; i < list.size(); i++) {
          if (i > 0) {
            result.append(", ");
          }
          DartType typeListPart = list.get(i);
          result.append(buildTypeText(element, typeListPart, specializations));
        }
        result.append(">");
      }
    }
    else {
      result.append("Function"); // functionType
    }

    return result.toString();
  }
}
