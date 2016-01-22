package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbStringLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbStringLiteralImpl extends HbPsiElementImpl implements HbStringLiteral {
  public HbStringLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
