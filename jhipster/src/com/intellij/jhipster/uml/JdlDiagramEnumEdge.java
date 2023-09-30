// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.jhipster.uml.model.JdlNodeData;
import org.jetbrains.annotations.NotNull;

final class JdlDiagramEnumEdge extends DiagramEdgeBase<JdlNodeData> {
  public JdlDiagramEnumEdge(@NotNull DiagramNode<JdlNodeData> source, @NotNull DiagramNode<JdlNodeData> target) {
    super(source, target, USE_ENUM);
  }

  static final DiagramRelationshipInfo USE_ENUM = (new DiagramRelationshipInfoAdapter.Builder()).setName("TO_ONE")
    .setLineType(DiagramLineType.DASHED)
    .setSourceArrow(DiagramRelationshipInfo.NONE)
    .setTargetArrow(DiagramRelationshipInfo.ANGLE)
    .create();
}
