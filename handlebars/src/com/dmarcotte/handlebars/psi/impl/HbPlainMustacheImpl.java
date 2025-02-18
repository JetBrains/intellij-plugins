// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPlainMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class HbPlainMustacheImpl extends HbMustacheImpl implements HbPlainMustache {
  HbPlainMustacheImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }
}
