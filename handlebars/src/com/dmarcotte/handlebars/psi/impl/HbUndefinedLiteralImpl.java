// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbUndefinedLiteral;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbUndefinedLiteralImpl extends HbPsiElementImpl implements HbUndefinedLiteral {
  public HbUndefinedLiteralImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
