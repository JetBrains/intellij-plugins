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

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class JSUmlEdge extends DiagramEdgeBase<Object> {
  public JSUmlEdge(DiagramNode<Object> source, DiagramNode<Object> target, @NotNull DiagramRelationshipInfo relationship) {
    super(source, target, relationship);
  }
}
