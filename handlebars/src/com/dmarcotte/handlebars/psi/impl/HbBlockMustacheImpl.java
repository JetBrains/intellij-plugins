// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
  public @Nullable HbMustacheName getBlockMustacheName() {
    return PsiTreeUtil.findChildOfType(this, HbMustacheName.class);
  }

  @Override
  public @Nullable String getName() {
    HbMustacheName mainPath = getBlockMustacheName();
    return mainPath == null ? null : mainPath.getName();
  }
}
