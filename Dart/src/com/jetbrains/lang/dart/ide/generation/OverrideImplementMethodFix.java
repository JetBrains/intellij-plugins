package com.jetbrains.lang.dart.ide.generation;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;

/**
 * @author: Fedor.Korotkov
 */
public class OverrideImplementMethodFix extends BaseCreateMethodsFix<DartComponent> {
  public OverrideImplementMethodFix(final DartClass haxeClass) {
    super(haxeClass);
  }

  @Override
  protected String buildFunctionsText(DartComponent element) {
    final StringBuilder result = new StringBuilder();
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(element, DartReturnType.class);
    final DartType dartType = returnType == null ? PsiTreeUtil.getChildOfType(element, DartType.class) : returnType.getType();
    if (dartType != null) {
      result.append(DartPresentableUtil.buildTypeText(element, dartType, specializations));
      result.append(" ");
    }
    if (element.isGetter() || element.isSetter()) {
      result.append(element.isGetter() ? "get " : "set ");
    }
    result.append(element.getName());
    result.append("(");
    result.append(DartPresentableUtil.getPresentableParameterList(element, specializations));
    result.append(")");
    result.append("{\n}\n");
    return result.toString();
  }
}
