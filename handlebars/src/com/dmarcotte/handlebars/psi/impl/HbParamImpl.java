package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbParam;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbParamImpl extends HbPsiElementImpl implements HbParam {
  public HbParamImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
