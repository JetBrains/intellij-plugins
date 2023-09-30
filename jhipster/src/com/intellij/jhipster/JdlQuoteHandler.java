// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.jhipster.psi.JdlTokenTypes;

final class JdlQuoteHandler extends SimpleTokenSetQuoteHandler {
  public JdlQuoteHandler() {
    super(JdlTokenTypes.DOUBLE_QUOTED_STRING);
  }
}
