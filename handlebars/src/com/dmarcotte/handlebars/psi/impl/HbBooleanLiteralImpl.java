package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbBooleanLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbBooleanLiteralImpl extends HbPsiElementImpl implements HbBooleanLiteral {
  public HbBooleanLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
