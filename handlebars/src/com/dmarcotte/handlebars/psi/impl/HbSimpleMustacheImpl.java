package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.HbIcons;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.dmarcotte.handlebars.psi.HbSimpleMustache;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbSimpleMustacheImpl extends HbMustacheImpl implements HbSimpleMustache {
  public HbSimpleMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    for (PsiElement childElement : getChildren()) {
      if (childElement instanceof HbMustacheName) {
        return ((HbMustacheName)childElement).getName();
      }
    }

    return null;
  }

  @Nullable
  @Override
  public Icon getIcon(@IconFlags int flags) {
    PsiElement openStacheElem = getFirstChild();
    if (openStacheElem == null) {
      return null;
    }

    if (openStacheElem.getNode().getElementType() == HbTokenTypes.OPEN_UNESCAPED) {
      return HbIcons.OPEN_UNESCAPED;
    }

    return HbIcons.OPEN_MUSTACHE;
  }
}
