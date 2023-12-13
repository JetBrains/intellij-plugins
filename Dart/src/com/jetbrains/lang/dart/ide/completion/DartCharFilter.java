package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartCharFilter extends CharFilter {
  @Nullable
  @Override
  public Result acceptChar(final char c, final int prefixLength, @NotNull final Lookup lookup) {
    if (!lookup.isCompletion()) return null;

    final PsiElement element = lookup.getPsiElement();
    final PsiElement parent = element == null ? null : element.getParent();
    if (parent instanceof DartStringLiteralExpression) {
      return Result.ADD_TO_PREFIX;
    }

    return null;
  }
}
