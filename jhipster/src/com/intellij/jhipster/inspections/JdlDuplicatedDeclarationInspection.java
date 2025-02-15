// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.JdlInspectionUtil;
import com.intellij.jhipster.psi.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class JdlDuplicatedDeclarationInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JdlVisitor() {
      @Override
      public void visitEntity(@NotNull JdlEntity o) {
        super.visitEntity(o);

        JdlEntityId entityId = o.getEntityId();
        if (entityId == null) return;

        String entityName = o.getName();

        Map<String, List<@NotNull JdlEntity>> map = JdlInspectionUtil.getAllEntities(holder.getFile());
        List<@NotNull JdlEntity> sameNameEntities = map.get(entityName);
        if (sameNameEntities != null && sameNameEntities.size() > 1) {
          holder.registerProblem(entityId, JdlBundle.message("inspection.message.duplicated.entity", entityName));
        }
      }

      @Override
      public void visitEnum(@NotNull JdlEnum o) {
        super.visitEnum(o);

        JdlEnumId enumId = o.getEnumId();
        if (enumId == null) return;

        String entityName = o.getName();

        Map<String, List<@NotNull JdlEnum>> map = JdlInspectionUtil.getAllEnums(holder.getFile());
        List<@NotNull JdlEnum> sameNameEnums = map.get(entityName);
        if (sameNameEnums != null && sameNameEnums.size() > 1) {
          holder.registerProblem(enumId, JdlBundle.message("inspection.message.duplicated.enum", entityName));
        }
      }
    };
  }
}
