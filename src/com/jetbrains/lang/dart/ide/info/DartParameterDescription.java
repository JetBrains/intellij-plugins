package com.jetbrains.lang.dart.ide.info;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartFormalParameterList;
import com.jetbrains.lang.dart.psi.DartNormalFormalParameter;
import com.jetbrains.lang.dart.util.DartGenericSpecialization;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartParameterDescription {
  private final String myText;

  public DartParameterDescription(@NotNull String text) {
    myText = text;
  }


  public static DartParameterDescription[] getParameters(DartComponent element, DartGenericSpecialization specialization) {
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    if (parameterList == null) {
      return new DartParameterDescription[0];
    }
    final List<DartNormalFormalParameter> list = parameterList.getNormalFormalParameterList();
    final DartParameterDescription[] result = new DartParameterDescription[list.size()];
    for (int i = 0, size = list.size(); i < size; i++) {
      result[i] = new DartParameterDescription(DartPresentableUtil.getPresentableNormalFormalParameter(list.get(i), specialization));
    }
    return result;
  }

  @Override
  public String toString() {
    return myText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartParameterDescription that = (DartParameterDescription)o;

    if (myText != null ? !myText.equals(that.myText) : that.myText != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myText != null ? myText.hashCode() : 0;
  }
}
