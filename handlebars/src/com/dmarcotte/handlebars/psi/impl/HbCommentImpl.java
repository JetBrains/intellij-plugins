package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbComment;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbCommentImpl extends HbPsiElementImpl implements HbComment {
  public HbCommentImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
