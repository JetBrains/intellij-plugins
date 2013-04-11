package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPsiElement;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import org.jetbrains.annotations.NotNull;

public class HbPsiElementImpl extends ASTWrapperPsiElement implements HbPsiElement {
  public HbPsiElementImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public ItemPresentation getPresentation() {
    return ItemPresentationProviders.getItemPresentation(this);
  }
}
