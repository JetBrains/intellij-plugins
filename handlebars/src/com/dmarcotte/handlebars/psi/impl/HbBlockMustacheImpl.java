package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbBlockMustache;
import com.dmarcotte.handlebars.psi.HbPath;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class HbBlockMustacheImpl extends HbPsiElementImpl implements HbBlockMustache {
  protected HbBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  @Nullable
  public HbPath getBlockMainPath() {
    return PsiTreeUtil.findChildOfType(this, HbPath.class);
  }

  @Override
  @Nullable
  public String getName() {
    HbPath mainPath = getBlockMainPath();
    return mainPath == null ? null : mainPath.getName();
  }
}
