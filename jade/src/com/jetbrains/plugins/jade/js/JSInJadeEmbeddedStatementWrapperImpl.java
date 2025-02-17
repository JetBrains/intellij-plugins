// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;


public class JSInJadeEmbeddedStatementWrapperImpl extends JSStatementImpl {
  public JSInJadeEmbeddedStatementWrapperImpl(IElementType elementType) {
    super(elementType);
  }
}