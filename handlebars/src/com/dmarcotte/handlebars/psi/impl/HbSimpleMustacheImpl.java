// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.dmarcotte.handlebars.psi.HbSimpleMustache;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbSimpleMustacheImpl extends HbPlainMustacheImpl implements HbSimpleMustache {
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

  @Override
  public @Nullable Icon getIcon(@IconFlags int flags) {
    PsiElement openStacheElem = getFirstChild();
    if (openStacheElem == null) {
      return null;
    }

    if (openStacheElem.getNode().getElementType() == HbTokenTypes.OPEN_UNESCAPED) {
      return HandlebarsIcons.Elements.OpenUnescaped;
    }

    return HandlebarsIcons.Elements.OpenMustache;
  }
}
