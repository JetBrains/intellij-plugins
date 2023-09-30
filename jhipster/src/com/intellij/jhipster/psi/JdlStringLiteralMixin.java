// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.jhipster.psi.impl.JdlValueImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public abstract class JdlStringLiteralMixin extends JdlValueImpl {
  public JdlStringLiteralMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void subtreeChanged() {
    putUserData(JdlPsiUtils.STRING_FRAGMENTS, null);
  }
}
