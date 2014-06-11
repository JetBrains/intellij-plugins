package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideImplementMethodFix extends BaseCreateMethodsFix<DartComponent> {
  public OverrideImplementMethodFix(final DartClass dartClass) {
    super(dartClass);
  }

  @Override
  @NotNull
  protected String getNothingFoundMessage() { return DartBundle.message("dart.fix.implement.none.found"); }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent element) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(element, DartReturnType.class);
    final DartType dartType = PsiTreeUtil.getChildOfType(element, DartType.class);
    if (returnType != null) {
      template.addTextSegment(DartPresentableUtil.buildTypeText(element, returnType, specializations));
      template.addTextSegment(" ");
    }
    else if (dartType != null) {
      template.addTextSegment(DartPresentableUtil.buildTypeText(element, dartType, specializations));
      template.addTextSegment(" ");
    }
    if (element.isGetter() || element.isSetter()) {
      template.addTextSegment(element.isGetter() ? "get " : "set ");
    }
    //noinspection ConstantConditions
    template.addTextSegment(element.getName());
    if (!element.isGetter()) {
      template.addTextSegment("(");
      template.addTextSegment(DartPresentableUtil.getPresentableParameterList(element, specializations));
      template.addTextSegment(")");
    }
    template.addTextSegment("{\n");
    template.addEndVariable();
    template.addTextSegment("\n}\n");
    return template;
  }


}
