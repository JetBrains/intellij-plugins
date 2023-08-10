package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbNullLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbNullLiteralImpl extends HbPsiElementImpl implements HbNullLiteral {
  public HbNullLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
