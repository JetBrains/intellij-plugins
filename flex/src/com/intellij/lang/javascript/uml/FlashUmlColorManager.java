/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class FlashUmlColorManager extends DiagramColorManagerBase {
  @NotNull
  @Override
  public Color getEdgeColor(@NotNull DiagramBuilder builder, @NotNull DiagramEdge edge) {
    return !isInterface(FlashUmlDataModel.getIdentifyingElement(edge.getSource())) ||
           !isInterface(FlashUmlDataModel.getIdentifyingElement(edge.getTarget())) ||
           edge.getRelationship().getStartArrow() != DiagramRelationshipInfo.DELTA ||
           edge.getRelationship().getLineType() != DiagramLineType.SOLID ?
           super.getEdgeColor(builder, edge) : ObjectUtils.notNull(builder.getColorScheme().getColor(DiagramColors.REALIZATION_EDGE_KEY));
  }

  private static boolean isInterface(Object element) {
    return element instanceof JSClass && ((JSClass)element).isInterface();
  }
}
