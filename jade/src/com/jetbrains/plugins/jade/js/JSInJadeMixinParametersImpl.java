// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class JSInJadeMixinParametersImpl extends JSStatementImpl implements JSExpression {
  public JSInJadeMixinParametersImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public JSExpression replace(@NotNull JSExpression other) {
    return JSChangeUtil.replaceExpression(this, other);
  }
}
