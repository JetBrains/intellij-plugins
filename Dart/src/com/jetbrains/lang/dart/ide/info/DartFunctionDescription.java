// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.info;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartFunctionDescription {
  private final String name;
  private final String returnType;
  @NotNull private final DartParameterDescription[] myParameters;
  @NotNull private final DartOptionalParameterDescription[] myOptionalParameters;

  public DartFunctionDescription(String name,
                                 String type,
                                 @NotNull DartParameterDescription[] parameters,
                                 @NotNull DartOptionalParameterDescription[] optionalParameters) {
    this.name = name;
    returnType = type;
    myParameters = parameters;
    myOptionalParameters = optionalParameters;
  }

  public String getName() {
    return name;
  }

  public String getReturnType() {
    return returnType;
  }

  @NotNull
  public DartParameterDescription[] getParameters() {
    return myParameters;
  }

  @NotNull
  public DartOptionalParameterDescription[] getOptionalParameters() {
    return myOptionalParameters;
  }

  public String getParametersListPresentableText() {
    final StringBuilder result = new StringBuilder();
    for (DartParameterDescription parameterDescription : myParameters) {
      if (result.length() > 0) {
        result.append(", ");
      }
      result.append(parameterDescription.toString());
    }


    if (myOptionalParameters.length > 0) {
      final String[] braces = myOptionalParameters[0].isPositional() ? new String[]{"[", "]"} : new String[]{"{", "}"};

      if (result.length() > 0) {
        result.append(", ");
      }
      result.append(braces[0]);

      for (int i = 0, length = myOptionalParameters.length; i < length; i++) {
        DartOptionalParameterDescription namedParameterDescription = myOptionalParameters[i];
        if (i > 0) {
          result.append(", ");
        }
        result.append(namedParameterDescription.toString());
      }

      result.append(braces[1]);
    }
    return result.toString();
  }

  public TextRange getParameterRange(int index) {
    if (index == -1) {
      return new TextRange(0, 0);
    }
    int startOffset = 0;
    for (int i = 0, length = myParameters.length; i < length; i++) {
      if (i == index) {
        int shift = i == 0 ? 0 : ", ".length();
        return new TextRange(startOffset + shift, startOffset + shift + myParameters[i].toString().length());
      }
      if (i > 0) {
        startOffset += ", ".length();
      }
      startOffset += myParameters[i].toString().length();
    }
    startOffset += myParameters.length > 0 ? ", [".length() : "[".length();

    for (int i = 0, length = myOptionalParameters.length; i < length; i++) {
      if ((i + myParameters.length) == index) {
        int shift = i == 0 ? 0 : ", ".length();
        return new TextRange(startOffset + shift, startOffset + shift + myOptionalParameters[i].toString().length());
      }
      if (i > 0) {
        startOffset += ", ".length();
      }
      startOffset += myOptionalParameters[i].toString().length();
    }
    return new TextRange(0, 0);
  }

  @Nullable
  public static DartFunctionDescription tryGetDescription(DartCallExpression callExpression) {
    final DartReference expression = (DartReference)callExpression.getExpression();
    PsiElement target = expression.resolve();
    PsiElement targetParent = target == null ? null : target.getParent();
    // If no target, such as "new Test()" or the target is a variable,
    // check if the expression's DartClass defines the "call" method.
    if (target == null || targetParent instanceof DartVarAccessDeclaration || targetParent instanceof DartGetterDeclaration) {
      DartClassResolveResult resolveResult = expression.resolveDartClass();
      DartClass dartClass = resolveResult.getDartClass();
      if (dartClass != null) {
        DartComponent callMethod = dartClass.findMethodByName("call");
        if (callMethod != null) {
          target = callMethod.getComponentName();
          targetParent = callMethod;
        }
      }
    }
    if (target instanceof DartComponentName && targetParent instanceof DartComponent) {
      final DartReference[] references = PsiTreeUtil.getChildrenOfType(expression, DartReference.class);
      final DartClassResolveResult resolveResult = (references != null && references.length == 2)
                                                   ? references[0].resolveDartClass()
                                                   : DartClassResolveResult
                                                     .create(PsiTreeUtil.getParentOfType(callExpression, DartClass.class));
      return createDescription((DartComponent)targetParent, resolveResult);
    }
    return null;
  }

  public static DartFunctionDescription createDescription(DartComponent namedComponent, DartClassResolveResult resolveResult) {
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(namedComponent, DartReturnType.class);
    final DartType dartType = PsiTreeUtil.getChildOfType(namedComponent, DartType.class);
    final String typeText = returnType == null
                            ? DartPresentableUtil.buildTypeText(namedComponent, dartType, resolveResult.getSpecialization())
                            : DartPresentableUtil.buildTypeText(namedComponent, returnType, resolveResult.getSpecialization());
    return new DartFunctionDescription(namedComponent.getName(), typeText,
                                       DartParameterDescription.getParameters(namedComponent, resolveResult.getSpecialization()),
                                       DartOptionalParameterDescription.getParameters(namedComponent, resolveResult.getSpecialization()));
  }
}
