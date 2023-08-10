package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbUndefinedLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbUndefinedLiteralImpl extends HbPsiElementImpl implements HbUndefinedLiteral {
  public HbUndefinedLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
