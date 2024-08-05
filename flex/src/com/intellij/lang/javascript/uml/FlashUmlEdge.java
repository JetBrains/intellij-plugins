// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class FlashUmlEdge extends DiagramEdgeBase<Object> {
  public FlashUmlEdge(DiagramNode<Object> source, DiagramNode<Object> target, @NotNull DiagramRelationshipInfo relationship) {
    super(source, target, relationship);
  }
}
