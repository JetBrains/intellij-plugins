// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.javascript.flex.completion.ActionScriptReferenceCompletionUtil;
import com.intellij.javascript.flex.refactoring.changeSignature.ActionScriptImportProcessor;
import com.intellij.lang.javascript.flex.ActionScriptExtensions;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.psi.JSElvisOwner;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureProcessor.RequiredImportProcessor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ActionScriptExtensionsImpl implements ActionScriptExtensions {
  @Override
  public @NotNull RequiredImportProcessor createImportProcessor() {
    return new ActionScriptImportProcessor();
  }

  @Override
  public @NotNull List<FormatFixer> optimizeImports(@NotNull PsiFile file) {
    return ECMAScriptImportOptimizer.executeNoFormat(file);
  }

  @Override
  public @NotNull Collection<@NotNull LookupElement> calcDefaultVariants(@NotNull JSElvisOwner expression,
                                                                         @NotNull PsiFile containingFile,
                                                                         @NotNull Set<@NotNull String> pushedSmartVariants,
                                                                         @NotNull CompletionParameters parameters,
                                                                         @NotNull CompletionResultSet resultSet) {
    return ActionScriptReferenceCompletionUtil.calcDefaultVariants(expression, containingFile, pushedSmartVariants, parameters, resultSet);
  }
}
