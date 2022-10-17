// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import com.intellij.plugins.drools.lang.psi.DroolsImportStatement;
import org.jetbrains.annotations.NotNull;

public abstract class DroolsImportImpl extends DroolsPsiCompositeElementImpl implements DroolsImportStatement {

  public DroolsImportImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getImportedClassName() {
    if (!isFunction()) {
      String text = getImportQualifier().getText();
      if (text != null && !text.endsWith(".*")) return text;
    }
    return null;
  }

  @Override
  public String getImportedPackage() {
    if (!isFunction()) {
      String text = getImportQualifier().getText();
      if (text != null && text.endsWith(".*")) return text.substring(0, text.lastIndexOf(".*"));
    }
    return null;
  }

  @Override
  public String getImportedFunction() {
    if (isFunction()) {
        return getImportQualifier().getText();
    }
    return null;
  }

  private boolean isFunction() {
    return  findChildByType(DroolsTokenTypes.FUNCTION) != null;
  }
}
