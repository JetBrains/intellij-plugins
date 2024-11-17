// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import com.intellij.tsr.psi.*;

import java.util.regex.Pattern;

final class TslAnnotator implements Annotator {

  private final Pattern ALL_UPPERCASE = Pattern.compile("[A-Z]+");

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof TslPropertyKey) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .textAttributes(TslSyntaxHighlighter.TSL_FIELD_NAME)
          .create();
    } else if (element instanceof TslObjectId) {
      if (ALL_UPPERCASE.matcher(element.getText()).matches()) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(TslSyntaxHighlighter.TSL_CONSTANT)
            .create();
      } else if (element.getParent() instanceof TslMapKey
          || element.getParent() instanceof TslPropertyKeyValue) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(TslSyntaxHighlighter.TSL_FIELD_NAME)
            .create();
      }
    } else if (element instanceof TslObjectName) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .textAttributes(TslSyntaxHighlighter.TSL_CLASSNAME)
          .create();
    } else if (element instanceof TslFallbackStringLiteral) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .textAttributes(TslSyntaxHighlighter.TSL_STRING)
          .create();
    }
  }
}
