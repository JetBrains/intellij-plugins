// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.JdlInspectionUtil;
import com.intellij.jhipster.psi.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@ApiStatus.Internal
public final class JdlUnusedDeclarationInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JdlVisitor() {
      @Override
      public void visitEntity(@NotNull JdlEntity o) {
        super.visitEntity(o);

        JdlEntityId entityId = o.getEntityId();
        if (entityId == null) return;

        Collection<@NotNull JdlEntity> usedEntities = JdlInspectionUtil.getUsedEntities(holder.getFile());
        if (!usedEntities.contains(o)) {
          holder.registerProblem(entityId, JdlBundle.message("inspection.message.unused.entity", o.getName()), ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
      }

      @Override
      public void visitEnum(@NotNull JdlEnum o) {
        super.visitEnum(o);

        JdlEnumId enumId = o.getEnumId();
        if (enumId == null) return;

        Collection<@NotNull JdlEnum> usedEnums = JdlInspectionUtil.getUsedEnums(holder.getFile());
        if (!usedEnums.contains(o)) {
          holder.registerProblem(enumId, JdlBundle.message("inspection.message.unused.enum", o.getName()), ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
      }
    };
  }
}
