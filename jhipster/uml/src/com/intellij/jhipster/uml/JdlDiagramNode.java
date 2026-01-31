// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.diagram.DiagramProvider;
import com.intellij.jhipster.uml.model.JdlNodeData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

final class JdlDiagramNode extends DiagramNodeBase<JdlNodeData> {

  private final JdlNodeData data;

  JdlDiagramNode(JdlNodeData data, DiagramProvider<JdlNodeData> provider) {
    super(provider);
    this.data = data;
  }

  @Override
  public @NotNull JdlNodeData getIdentifyingElement() {
    return data;
  }

  @Override
  public @Nullable @Nls String getTooltip() {
    return null;
  }

  @Override
  public @Nullable Icon getIcon() {
    return data.getIcon();
  }
}
