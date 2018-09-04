// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.highlighting;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptHighlightingLexer;
import org.jetbrains.annotations.NotNull;

public class Angular2HighlightingLexer extends JavaScriptHighlightingLexer {

  public Angular2HighlightingLexer(@NotNull DialectOptionHolder optionHolder) {
    super(optionHolder);
  }
}
