// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.model.*;
import com.intellij.jhipster.psi.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class JdlIncorrectOptionTypeInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JdlVisitor() {
      @Override
      public void visitOptionNameValue(@NotNull JdlOptionNameValue o) {
        super.visitOptionNameValue(o);

        JdlValue actualValue = o.getValue();
        if (actualValue == null) return;

        String optionName = o.getName();

        JdlOptionModel model = JdlOptionModel.INSTANCE;

        Map<String, JdlOptionMapping> options;
        if (o.getParent() instanceof JdlConfigBlock) {
          options = model.getApplicationConfigOptions();
        }
        else if (o.getParent() instanceof JdlDeployment) {
          options = model.getDeploymentOptions();
        }
        else {
          return;
        }

        JdlOptionMapping optionMapping = options.get(optionName);
        if (optionMapping == null) return;

        JdlOptionType expectedType = optionMapping.getPropertyType();

        boolean matches = true;
        String expectedComment = "";

        if (expectedType == JdlPrimitiveType.BOOLEAN_TYPE) {
          matches = actualValue instanceof JdlBooleanLiteral;
          expectedComment = expectedType.getName();
        }
        else if (expectedType == JdlPrimitiveType.INTEGER_TYPE) {
          matches = actualValue instanceof JdlNumberLiteral;
          expectedComment = expectedType.getName();
        }
        else if (expectedType == JdlPrimitiveType.STRING_ARRAY_TYPE) {
          matches = actualValue instanceof JdlArrayLiteral;
          expectedComment = expectedType.getName();
        }
        else if (optionMapping instanceof JdlEnumMapping) {
          List<String> supportedOptions = ((JdlEnumMapping)optionMapping).getOptions();
          matches = (actualValue instanceof JdlId || actualValue instanceof JdlBooleanLiteral)
                    && supportedOptions.contains(actualValue.getText());

          if (!matches) {
            expectedComment = JdlBundle.message("inspection.message.incorrect.value.type.one.of", "[" + String.join(", ", supportedOptions) + "]");
          }
        }

        if (!matches) {
          holder.registerProblem(actualValue, JdlBundle.message("inspection.message.incorrect.value.type.expected", expectedComment));
        }
      }
    };
  }
}
