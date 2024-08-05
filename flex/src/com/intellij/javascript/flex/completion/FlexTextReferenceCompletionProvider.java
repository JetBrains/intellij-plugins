// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.LegacyCompletionContributor;
import com.intellij.lang.javascript.completion.JSTextReferenceCompletionProvider;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptTextReference;
import com.intellij.lang.javascript.psi.impl.JSTextReference;
import org.jetbrains.annotations.NotNull;

public final class FlexTextReferenceCompletionProvider extends CompletionContributor {
  @Override
  public void fillCompletionVariants(final @NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    LegacyCompletionContributor.processReferences(parameters, result, (reference, set) -> {
      if (reference instanceof ActionScriptTextReference) {
        JSTextReferenceCompletionProvider.addVariants((JSTextReference)reference, parameters, set);
      }
    });
  }
}