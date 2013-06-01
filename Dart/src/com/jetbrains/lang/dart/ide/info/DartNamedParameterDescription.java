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

  public DartNamedParameterDescription(@NotNull String text, @Nullable String value) {
    myText = text;
    myValue = value;
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
      final String normalFormalParameter =
        DartPresentableUtil.getPresentableNormalFormalParameter(list.get(i).getNormalFormalParameter(), specialization);
      final PsiElement valueElement = list.get(i).getExpression();
      result[i] = new DartNamedParameterDescription(normalFormalParameter, valueElement == null ? null : valueElement.getText());
    }
    return result;
  }

  @Override
  public String toString() {
    if (myValue != null) {
      return myText + " = " + myValue;
    }
    return myText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartNamedParameterDescription that = (DartNamedParameterDescription)o;

    if (myText != null ? !myText.equals(that.myText) : that.myText != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myText != null ? myText.hashCode() : 0;
  }
}
