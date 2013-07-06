package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbMustacheNameImpl extends HbPsiElementImpl implements HbMustacheName {
  public HbMustacheNameImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getText();
  }
}
