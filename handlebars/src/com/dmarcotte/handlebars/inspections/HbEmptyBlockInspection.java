// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.inspections;


import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.dmarcotte.handlebars.psi.HbParam;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class HbEmptyBlockInspection extends LocalInspectionTool {
  private static final Set<String> HELPERS_WITH_ARGUMENTS = Set.of("if", "each", "with");

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (!(element instanceof HbOpenBlockMustache)) {
          return;
        }

        String name = ((HbOpenBlockMustache)element).getName();
        if (name != null && HELPERS_WITH_ARGUMENTS.contains(name) &&
            null == PsiTreeUtil.getChildrenOfType(element, HbParam.class)) {
          holder.registerProblem(element, HbBundle.message("hb.block.mismatch.inspection.empty.block", name));
        }
      }
    };
  }
}
