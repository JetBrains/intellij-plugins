// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.tsr.psi.TslTokenSets;

final class TslQuoteHandler extends SimpleTokenSetQuoteHandler {
  public TslQuoteHandler() {
    super(TslTokenSets.STRINGS);
  }
}
