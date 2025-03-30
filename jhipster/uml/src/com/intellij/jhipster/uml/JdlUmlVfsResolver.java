// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.jhipster.uml.model.JdlNodeData;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JdlUmlVfsResolver implements DiagramVfsResolver<JdlNodeData> {
  @Override
  public @Nullable String getQualifiedName(@Nullable JdlNodeData data) {
    if (data == null) return null;

    String name = data.getName();
    return name != null ? name : "";
  }

  @Override
  public @Nullable JdlNodeData resolveElementByFQN(@NotNull String s, @NotNull Project project) {
    return null;
  }
}
