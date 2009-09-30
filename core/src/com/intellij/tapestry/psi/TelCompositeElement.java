/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TelCompositeElement extends ASTWrapperPsiElement {

  public TelCompositeElement(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
      return getNode().getElementType().toString();
  }
}
