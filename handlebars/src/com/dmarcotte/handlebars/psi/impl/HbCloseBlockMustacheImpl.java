package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class HbCloseBlockMustacheImpl extends HbBlockMustacheImpl implements HbCloseBlockMustache {
  public HbCloseBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public HbOpenBlockMustache getPairedElement() {
    PsiElement openBlockElement = getParent().getFirstChild();
    if (openBlockElement instanceof HbOpenBlockMustache) {
      return (HbOpenBlockMustache)openBlockElement;
    }

    return null;
  }
}
