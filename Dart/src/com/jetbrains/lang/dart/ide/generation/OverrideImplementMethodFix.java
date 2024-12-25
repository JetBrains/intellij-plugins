// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

public class OverrideImplementMethodFix extends BaseCreateMethodsFix<DartComponent> {
  final boolean myImplementNotOverride;

  public OverrideImplementMethodFix(final @NotNull DartClass dartClass, final boolean implementNotOverride) {
    super(dartClass);
    myImplementNotOverride = implementNotOverride;
  }

  @Override
  protected @NotNull @NlsContexts.Command String getCommandName() {
    return myImplementNotOverride ? DartBundle.message("command.implement.methods")
                                  : DartBundle.message("command.override.methods");
  }

  @Override
  protected @NotNull String getNothingFoundMessage() {
    return DartBundle.message(myImplementNotOverride ? "dart.fix.implement.none.found" : "dart.fix.override.none.found");
  }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent element) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);
    template.addTextSegment("@override\n");
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