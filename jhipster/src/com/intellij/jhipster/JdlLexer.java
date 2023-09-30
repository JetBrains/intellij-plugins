// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.jhipster.lexer._JdlLexer;
import com.intellij.lexer.FlexAdapter;

public final class JdlLexer extends FlexAdapter {
  public JdlLexer() {
    super(new _JdlLexer(null));
  }
}
