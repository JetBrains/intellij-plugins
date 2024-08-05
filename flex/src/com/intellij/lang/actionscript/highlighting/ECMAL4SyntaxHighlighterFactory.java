// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

public final class ECMAL4SyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Override
  protected @NotNull SyntaxHighlighter createHighlighter() {
    return new ECMAL4Highlighter();
  }
}
