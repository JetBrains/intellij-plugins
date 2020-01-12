/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TelArgumentList extends TelCompositeElement {
  public TelArgumentList(final ASTNode node) {
    super(node);
  }

  public TelExpression @NotNull [] getArguments() {
    return findChildrenByClass(TelExpression.class);
  }
}