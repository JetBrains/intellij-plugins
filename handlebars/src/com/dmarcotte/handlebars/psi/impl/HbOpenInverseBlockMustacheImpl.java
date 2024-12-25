// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbOpenInverseBlockMustache;
import com.intellij.lang.ASTNode;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbOpenInverseBlockMustacheImpl extends HbOpenBlockMustacheImpl implements HbOpenInverseBlockMustache {
  public HbOpenInverseBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public @Nullable Icon getIcon(int flags) {
    return HandlebarsIcons.Elements.OpenInverse;
  }
}
