package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbBlockMustache;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class HbBlockMustacheImpl extends HbMustacheImpl implements HbBlockMustache {
  protected HbBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  @Nullable
  public HbMustacheName getBlockMustacheName() {
    return PsiTreeUtil.findChildOfType(this, HbMustacheName.class);
  }

  @Override
  @Nullable
  public String getName() {
    HbMustacheName mainPath = getBlockMustacheName();
    return mainPath == null ? null : mainPath.getName();
  }
}
