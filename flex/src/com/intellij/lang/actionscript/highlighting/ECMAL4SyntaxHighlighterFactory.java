// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

public class ECMAL4SyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Override
  @NotNull
  protected SyntaxHighlighter createHighlighter() {
    return new ECMAL4Highlighter();
  }
}
