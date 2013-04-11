package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.HbIcons;
import com.dmarcotte.handlebars.psi.HbPartial;
import com.dmarcotte.handlebars.psi.HbPartialName;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbPartialImpl extends HbMustacheImpl implements HbPartial {
  public HbPartialImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    HbPartialName partialName = PsiTreeUtil.findChildOfType(this, HbPartialName.class);
    return partialName == null ? null : partialName.getName();
  }

  @Nullable
  @Override
  public Icon getIcon(@IconFlags int flags) {
    return HbIcons.OPEN_PARTIAL;
  }
}
