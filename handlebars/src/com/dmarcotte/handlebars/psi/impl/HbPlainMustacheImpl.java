package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPlainMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

class HbPlainMustacheImpl extends HbMustacheImpl implements HbPlainMustache {
  HbPlainMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
