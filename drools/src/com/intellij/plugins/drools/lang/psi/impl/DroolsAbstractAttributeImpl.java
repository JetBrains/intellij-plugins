// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsAttribute;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsAbstractAttributeImpl extends DroolsPsiCompositeElementImpl implements DroolsAttribute {
  public DroolsAbstractAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public String getAttributeName() {
    return getFirstChild().getText();
  }
}
