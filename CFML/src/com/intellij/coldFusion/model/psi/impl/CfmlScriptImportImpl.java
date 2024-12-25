// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.coldFusion.model.psi.CfmlStringLiteralExpression;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlScriptImportImpl extends CfmlCompositeElement implements CfmlImport {
  public CfmlScriptImportImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isImported(String componentName) {
    String importString = getImportString();
    return importString != null ? importString.endsWith(componentName) : false;
  }

  @Override
  public @Nullable String getImportString() {
    CfmlStringLiteralExpression childByType = findChildByClass(CfmlStringLiteralExpression.class);
    if (childByType != null) {
      return childByType.getValue();
    }
    return null;
  }

  @Override
  public String getPrefix() {
    return null;
  }

  /*
  @NotNull
  @Override
  public PsiReference[] getReferences() {
    PsiElement reference = findChildByType(CfscriptElementTypes.COMPONENT_REFERENCE);
    if (reference != null) {
      return new PsiReference[]{new CfmlComponentReference( reference.getNode(), this)};
    }
    return super.getReferences();
  }
  */
}
