package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbBlockWrapper;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbBlockWrapperImpl extends HbPsiElementImpl implements HbBlockWrapper {
  public HbBlockWrapperImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    HbOpenBlockMustache openBlockMustache = getHbOpenBlockMustache();
    return openBlockMustache == null ? null : openBlockMustache.getName();
  }

  @Nullable
  @Override
  public Icon getIcon(@IconFlags int flags) {
    HbOpenBlockMustache openBlockMustache = getHbOpenBlockMustache();
    return openBlockMustache == null ? null : openBlockMustache.getIcon(0);
  }

  private HbOpenBlockMustache getHbOpenBlockMustache() {
    return PsiTreeUtil.findChildOfType(this, HbOpenBlockMustache.class);
  }
}
