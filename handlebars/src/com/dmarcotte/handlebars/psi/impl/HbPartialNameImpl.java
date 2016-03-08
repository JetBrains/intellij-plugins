package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPartialName;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbPartialNameImpl extends HbPsiElementImpl implements HbPartialName {
  public HbPartialNameImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getText();
  }
}
