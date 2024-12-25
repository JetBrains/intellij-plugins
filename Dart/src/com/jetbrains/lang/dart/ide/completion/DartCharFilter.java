// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartCharFilter extends CharFilter {
  @Override
  public @Nullable Result acceptChar(final char c, final int prefixLength, final @NotNull Lookup lookup) {
    if (!lookup.isCompletion()) return null;

    final PsiElement element = lookup.getPsiElement();
    final PsiElement parent = element == null ? null : element.getParent();
    if (parent instanceof DartStringLiteralExpression) {
      return Result.ADD_TO_PREFIX;
    }

    return null;
  }
}
