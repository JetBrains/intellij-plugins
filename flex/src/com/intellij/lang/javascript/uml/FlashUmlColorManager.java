// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class FlashUmlColorManager extends DiagramColorManagerBase {
  @NotNull
  @Override
  public Color getEdgeColor(@NotNull DiagramBuilder builder, @NotNull DiagramEdge edge) {
    return !isInterface(FlashUmlDataModel.getIdentifyingElement(edge.getSource())) ||
           !isInterface(FlashUmlDataModel.getIdentifyingElement(edge.getTarget())) ||
           edge.getRelationship().getTargetArrow() != DiagramRelationshipInfo.DELTA ||
           edge.getRelationship().getLineType() != DiagramLineType.SOLID ?
           super.getEdgeColor(builder, edge) : Objects.requireNonNull(builder.getColorScheme().getColor(DiagramColors.REALIZATION_EDGE));
  }

  private static boolean isInterface(Object element) {
    return element instanceof JSClass && ((JSClass)element).isInterface();
  }
}
