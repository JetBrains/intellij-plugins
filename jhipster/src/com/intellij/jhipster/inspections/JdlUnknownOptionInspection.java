// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.model.JdlOptionMapping;
import com.intellij.jhipster.model.JdlOptionModel;
import com.intellij.jhipster.psi.JdlConfigBlock;
import com.intellij.jhipster.psi.JdlDeployment;
import com.intellij.jhipster.psi.JdlOptionNameValue;
import com.intellij.jhipster.psi.JdlVisitor;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ApiStatus.Internal
public final class JdlUnknownOptionInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JdlVisitor() {
      @Override
      public void visitOptionNameValue(@NotNull JdlOptionNameValue o) {
        super.visitOptionNameValue(o);

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

        if (!options.containsKey(optionName)) {
          holder.registerProblem(o.getNameElement(), JdlBundle.message("inspection.message.unknown.option", optionName));
        }
      }
    };
  }
}
