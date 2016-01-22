package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbNumberLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbNumberLiteralImpl extends HbPsiElementImpl implements HbNumberLiteral {
  public HbNumberLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
