// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.lexer.FlexAdapter;
import com.intellij.tsr.lexer._TslLexer;

public final class TslLexer extends FlexAdapter {
  public TslLexer() {
    super(new _TslLexer(null));
  }
}