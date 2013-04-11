package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPath;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbPathImpl extends HbPsiElementImpl implements HbPath {
  public HbPathImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getText();
  }
}
