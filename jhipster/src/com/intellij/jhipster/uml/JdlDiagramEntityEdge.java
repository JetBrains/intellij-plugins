// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.jhipster.uml.model.JdlEntityNodeLinkType;
import com.intellij.jhipster.uml.model.JdlNodeData;
import org.jetbrains.annotations.NotNull;

final class JdlDiagramEntityEdge extends DiagramEdgeBase<JdlNodeData> {
  JdlDiagramEntityEdge(@NotNull DiagramNode<JdlNodeData> source,
                       @NotNull DiagramNode<JdlNodeData> target,
                       @NotNull JdlEntityNodeLinkType linkType) {
    super(source, target, toRelationshipInfo(linkType));
  }

  private static DiagramRelationshipInfo toRelationshipInfo(JdlEntityNodeLinkType linkType) {
    return switch (linkType) {
      case ONE_TO_ONE -> JdlDiagramEntityEdge.ONE_TO_ONE;
      case MANY_TO_MANY -> JdlDiagramEntityEdge.MANY_TO_MANY;
      case MANY_TO_ONE -> JdlDiagramEntityEdge.MANY_TO_ONE;
      case ONE_TO_MANY -> JdlDiagramEntityEdge.ONE_TO_MANY;
    };
  }

  static final DiagramRelationshipInfo ONE_TO_ONE = (new DiagramRelationshipInfoAdapter.Builder()).setName("ONE_TO_ONE")
    .setLineType(DiagramLineType.SOLID)
    .setSourceArrow(DiagramRelationshipInfo.ANGLE)
    .setTargetArrow(DiagramRelationshipInfo.ANGLE)
    .setUpperTargetLabel("1")
    .setUpperSourceLabel("1")
    .create();

  static final DiagramRelationshipInfo ONE_TO_MANY = (new DiagramRelationshipInfoAdapter.Builder()).setName("ONE_TO_MANY")
    .setLineType(DiagramLineType.SOLID)
    .setSourceArrow(DiagramRelationshipInfo.DIAMOND)
    .setTargetArrow(DiagramRelationshipInfo.ANGLE)
    .setUpperTargetLabel("*")
    .setUpperSourceLabel("1")
    .create();

  static final DiagramRelationshipInfo MANY_TO_ONE = (new DiagramRelationshipInfoAdapter.Builder()).setName("MANY_TO_ONE")
    .setLineType(DiagramLineType.SOLID)
    .setSourceArrow(DiagramRelationshipInfo.ANGLE)
    .setTargetArrow(DiagramRelationshipInfo.DIAMOND)
    .setUpperTargetLabel("1")
    .setUpperSourceLabel("*")
    .create();

  static final DiagramRelationshipInfo MANY_TO_MANY = (new DiagramRelationshipInfoAdapter.Builder()).setName("MANY_TO_MANY")
    .setLineType(DiagramLineType.SOLID)
    .setSourceArrow(DiagramRelationshipInfo.DIAMOND)
    .setTargetArrow(DiagramRelationshipInfo.DIAMOND)
    .setUpperTargetLabel("*")
    .setUpperSourceLabel("*")
    .create();
}
