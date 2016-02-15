package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideImplementMethodFix extends BaseCreateMethodsFix<DartComponent> {
  final boolean myImplementNotOverride;

  public OverrideImplementMethodFix(@NotNull final DartClass dartClass, final boolean implementNotOverride) {
    super(dartClass);
    myImplementNotOverride = implementNotOverride;
  }

  @Override
  @NotNull
  protected String getNothingFoundMessage() {
    return myImplementNotOverride
           ? DartBundle.message("dart.fix.implement.none.found")
           : DartBundle.message("dart.fix.override.none.found");
  }


  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent element) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);
    if (CodeStyleSettingsManager.getSettings(element.getProject()).INSERT_OVERRIDE_ANNOTATION) {
      template.addTextSegment("@override\n");
    }
    final boolean isField = element instanceof DartVarAccessDeclaration || element instanceof DartVarDeclarationListPart;
    if (isField && element.isFinal()) {
      template.addTextSegment("final");
      template.addTextSegment(" ");
    }
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

    if (isField) {
      if (returnType == null && dartType == null) {
        template.addTextSegment("var");
        template.addTextSegment(" ");
      }
      //noinspection ConstantConditions
      template.addTextSegment(element.getName());
      if (element.isFinal()) {
        template.addTextSegment(" ");
        template.addTextSegment("=");
        template.addTextSegment(" ");
        template.addTextSegment("null");
      }
      template.addTextSegment("; "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
      return template;
    }

    if (element.isOperator()) {
      template.addTextSegment("operator ");
    }

    if (element.isGetter() || element.isSetter()) {
      template.addTextSegment(element.isGetter() ? "get " : "set ");
    }
    //noinspection ConstantConditions
    template.addTextSegment(element.getName());
    if (!element.isGetter()) {
      template.addTextSegment("(");
      template.addTextSegment(DartPresentableUtil.getPresentableParameterList(element, specializations, false, true, true));
      template.addTextSegment(")");
    }
    template.addTextSegment("{\n");
    template.addEndVariable();
    template.addTextSegment("\n} "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
    return template;
  }
}