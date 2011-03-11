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

import com.intellij.diagram.DiagramEdge;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.diagram.DiagramColorManagerBase;

import java.awt.*;

public class JSUmlColorManager extends DiagramColorManagerBase {
  @Override
  public Color getEdgeColor(DiagramEdge edge) {
    return edge == null
        || !isInterface(JSUmlDataModel.getIdentifyingElement(edge.getSource()))
        || !isInterface(JSUmlDataModel.getIdentifyingElement(edge.getTarget()))
        || edge.getRelationship().getStartArrow() != DiagramRelationshipInfo.DELTA
        || edge.getRelationship().getLineType() != DiagramLineType.SOLID ?
    super.getEdgeColor(edge) : REALIZATION;
  }

  private static boolean isInterface(Object element) {
    return element instanceof JSClass && ((JSClass)element).isInterface();
  }
}
