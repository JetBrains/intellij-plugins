package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbHelperName;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbHelperNameImpl extends HbPsiElementImpl implements HbHelperName {
  public HbHelperNameImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    return getText();
  }
}
