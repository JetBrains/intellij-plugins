package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.HbIcons;
import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbOpenBlockMustacheImpl extends HbBlockMustacheImpl implements HbOpenBlockMustache {
  public HbOpenBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public HbCloseBlockMustache getPairedElement() {
    PsiElement closeBlockElement = getParent().getLastChild();
    if (closeBlockElement instanceof HbCloseBlockMustache) {
      return (HbCloseBlockMustache)closeBlockElement;
    }

    return null;
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return HbIcons.OPEN_BLOCK;
  }
}
