package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbData;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbDataImpl extends HbPsiElementImpl implements HbData {
  public HbDataImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getText();
  }
}
