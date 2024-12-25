// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPartial;
import com.dmarcotte.handlebars.psi.HbPartialName;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbPartialImpl extends HbPlainMustacheImpl implements HbPartial {
  public HbPartialImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    for (PsiElement childElement : getChildren()) {
      if (childElement instanceof HbPartialName) {
        return ((HbPartialName)childElement).getName();
      }
    }

    return null;
  }

  @Override
  public @Nullable Icon getIcon(@IconFlags int flags) {
    return HandlebarsIcons.Elements.OpenPartial;
  }
}
