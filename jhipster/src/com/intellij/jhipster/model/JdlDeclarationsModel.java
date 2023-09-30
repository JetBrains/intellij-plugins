// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import com.intellij.jhipster.psi.JdlConstant;
import com.intellij.jhipster.psi.JdlEntity;
import com.intellij.jhipster.psi.JdlEnum;
import com.intellij.jhipster.psi.JdlVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class JdlDeclarationsModel {
  private JdlDeclarationsModel() {
  }

  public static Collection<JdlEnum> findAllEnums(PsiFile file) {
    List<JdlEnum> enums = new ArrayList<>();

    file.acceptChildren(new JdlVisitor() {
      @Override
      public void visitEnum(@NotNull JdlEnum o) {
        super.visitEnum(o);

        enums.add(o);
      }
    });

    return enums;
  }

  public static Collection<JdlEntity> findAllEntities(PsiFile file) {
    List<JdlEntity> entities = new ArrayList<>();

    file.acceptChildren(new JdlVisitor() {
      @Override
      public void visitEntity(@NotNull JdlEntity o) {
        super.visitEntity(o);

        entities.add(o);
      }
    });

    return entities;
  }

  public static Collection<JdlConstant> findAllJdlConstants(PsiFile file) {
    List<JdlConstant> entities = new ArrayList<>();

    file.acceptChildren(new JdlVisitor() {
      @Override
      public void visitConstant(@NotNull JdlConstant o) {
        super.visitConstant(o);

        entities.add(o);
      }
    });

    return entities;
  }
}
