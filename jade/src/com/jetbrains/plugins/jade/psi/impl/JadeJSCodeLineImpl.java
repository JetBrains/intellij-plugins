// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeJSCodeLineImpl extends CompositePsiElement {
  public JadeJSCodeLineImpl() {
    super(JadeElementTypes.JS_CODE_LINE);
  }

  public boolean shouldIndent() {
    return true;
  }
}
