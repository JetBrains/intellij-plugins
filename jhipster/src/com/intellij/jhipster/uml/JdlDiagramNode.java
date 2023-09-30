// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramProvider;
import com.intellij.jhipster.uml.model.JdlNodeData;
import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class JdlDiagramNode extends UserDataHolderBase implements DiagramNode<JdlNodeData> {

  private final JdlNodeData data;
  private final DiagramProvider<JdlNodeData> provider;

  public JdlDiagramNode(JdlNodeData data, DiagramProvider<JdlNodeData> provider) {
    this.data = data;
    this.provider = provider;
  }

  @Override
  public @NotNull JdlNodeData getIdentifyingElement() {
    return data;
  }

  @Override
  public @NotNull DiagramProvider<JdlNodeData> getProvider() {
    return provider;
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
