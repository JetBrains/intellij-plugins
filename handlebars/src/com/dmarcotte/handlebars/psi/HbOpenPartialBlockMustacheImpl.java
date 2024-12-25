// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi;

import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl;
import com.intellij.lang.ASTNode;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbOpenPartialBlockMustacheImpl  extends HbOpenBlockMustacheImpl implements HbOpenPartialBlockMustache {
  public HbOpenPartialBlockMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public @Nullable Icon getIcon(int flags) {
    // todo this icon is okay since this _is_ a partial, but a proper `{{#>` would probably be better
    return HandlebarsIcons.Elements.OpenPartial;
  }
}
