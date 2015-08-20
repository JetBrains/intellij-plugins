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

  @Nullable
  @Override
  public String getHashName() {
    final PsiElement element = getHashNameElement();
    return element == null ? null : element.getText();
  }

  @Nullable
  @Override
  public PsiElement getHashNameElement() {
    final ASTNode idNode = getNode().findChildByType(HbTokenTypes.ID);
    return idNode == null ? null : idNode.getPsi();
  }
}
