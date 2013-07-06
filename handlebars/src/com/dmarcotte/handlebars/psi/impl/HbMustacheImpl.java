package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbMustacheImpl extends HbPsiElementImpl implements HbMustache {
  public HbMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
