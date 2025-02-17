// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class JadeSimpleInterpolationLexer extends JadeBaseInterpolationLexer {
  public JadeSimpleInterpolationLexer(@NotNull Lexer delegate) {
    super(delegate);
  }

  @Override
  protected CharSequence getSubstitutionForInterpolation(CharSequence buffer, int start, int end, TextRange interpolationRange) {
    return StringUtil.repeatSymbol('a', interpolationRange.getLength());
  }
}
