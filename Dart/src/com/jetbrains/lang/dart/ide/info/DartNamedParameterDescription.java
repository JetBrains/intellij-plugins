package com.jetbrains.lang.dart.ide.info;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartDefaultFormalNamedParameter;
import com.jetbrains.lang.dart.psi.DartFormalParameterList;
import com.jetbrains.lang.dart.psi.DartNamedFormalParameters;
import com.jetbrains.lang.dart.util.DartGenericSpecialization;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartNamedParameterDescription {
  private final String myText;
  @Nullable
  private final String myValue;
  private boolean myIsPositional;

  public DartNamedParameterDescription(@NotNull String text, @Nullable String value, boolean isPositional) {
    myText = text;
    myValue = value;
    myIsPositional = isPositional;
  }

  public static DartNamedParameterDescription[] getParameters(DartComponent element, DartGenericSpecialization specialization) {
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    final DartNamedFormalParameters namedFormalParameters = parameterList == null ? null : parameterList.getNamedFormalParameters();
    if (namedFormalParameters == null) {
      return new DartNamedParameterDescription[0];
    }
    final List<DartDefaultFormalNamedParameter> list = namedFormalParameters.getDefaultFormalNamedParameterList();
    final DartNamedParameterDescription[] result = new DartNamedParameterDescription[list.size()];
    for (int i = 0, size = list.size(); i < size; i++) {
      final DartDefaultFormalNamedParameter formalNamedParameter = list.get(i);
      final String normalFormalParameter =
        DartPresentableUtil.getPresentableNormalFormalParameter(formalNamedParameter.getNormalFormalParameter(), specialization);
      final PsiElement valueElement = formalNamedParameter.getExpression();
      result[i] =
        new DartNamedParameterDescription(normalFormalParameter, valueElement == null ? null : valueElement.getText(), isPositional(
          formalNamedParameter));
    }
    return result;
  }

  private static boolean isPositional(final DartDefaultFormalNamedParameter parameter) {
    final DartNamedFormalParameters formalParameters = PsiTreeUtil.getParentOfType(parameter, DartNamedFormalParameters.class);
    if (formalParameters == null) {
      return false;
    }

    final PsiElement firstChild = formalParameters.getFirstChild();
    return firstChild != null && "[".equals(firstChild.getText());
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

    DartNamedParameterDescription that = (DartNamedParameterDescription)o;

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
