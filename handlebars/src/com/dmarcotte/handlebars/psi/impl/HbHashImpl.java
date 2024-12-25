// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbHash;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class HbHashImpl extends HbPsiElementImpl implements HbHash {
  public HbHashImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public @Nullable String getHashName() {
    final PsiElement element = getHashNameElement();
    return element == null ? null : element.getText();
  }

  @Override
  public @Nullable PsiElement getHashNameElement() {
    final ASTNode idNode = getNode().findChildByType(HbTokenTypes.ID);
    return idNode == null ? null : idNode.getPsi();
  }
}
