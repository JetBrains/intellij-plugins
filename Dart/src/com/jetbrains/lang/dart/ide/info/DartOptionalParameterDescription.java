// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.info;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartDefaultFormalNamedParameter;
import com.jetbrains.lang.dart.psi.DartFormalParameterList;
import com.jetbrains.lang.dart.psi.DartOptionalFormalParameters;
import com.jetbrains.lang.dart.util.DartGenericSpecialization;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartOptionalParameterDescription {
  private final @NotNull String myText;
  private final @Nullable String myValue;
  private final boolean myIsPositional;

  public DartOptionalParameterDescription(@NotNull String text, @Nullable String value, boolean isPositional) {
    myText = text;
    myValue = value;
    myIsPositional = isPositional;
  }

  public static DartOptionalParameterDescription[] getParameters(DartComponent element, DartGenericSpecialization specialization) {
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    final DartOptionalFormalParameters optionalFormalParameters =
      parameterList == null ? null : parameterList.getOptionalFormalParameters();
    if (optionalFormalParameters == null) {
      return new DartOptionalParameterDescription[0];
    }
    final List<DartDefaultFormalNamedParameter> list = optionalFormalParameters.getDefaultFormalNamedParameterList();
    final DartOptionalParameterDescription[] result = new DartOptionalParameterDescription[list.size()];
    for (int i = 0, size = list.size(); i < size; i++) {
      final DartDefaultFormalNamedParameter formalNamedParameter = list.get(i);
      final String normalFormalParameter =
        DartPresentableUtil.getPresentableNormalFormalParameter(formalNamedParameter.getNormalFormalParameter(), specialization);
      final PsiElement valueElement = formalNamedParameter.getExpression();
      result[i] =
        new DartOptionalParameterDescription(normalFormalParameter, valueElement == null ? null : valueElement.getText(), isPositional(
          formalNamedParameter));
    }
    return result;
  }

  private static boolean isPositional(final DartDefaultFormalNamedParameter parameter) {
    final DartOptionalFormalParameters formalParameters = PsiTreeUtil.getParentOfType(parameter, DartOptionalFormalParameters.class);
    if (formalParameters == null) {
      return false;
    }

    final PsiElement firstChild = formalParameters.getFirstChild();
    return firstChild != null && "[".equals(firstChild.getText());
  }

  public @NotNull String getText() {
    return myText;
  }

  @Override
  public String toString() {
    if (myValue == null) return myText;
    return myText + (isPositional() ? " = " : ": ") + myValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartOptionalParameterDescription that = (DartOptionalParameterDescription)o;

    if (myText.equals(that.myText)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myText.hashCode();
  }

  public boolean isPositional() {
    return myIsPositional;
  }
}
