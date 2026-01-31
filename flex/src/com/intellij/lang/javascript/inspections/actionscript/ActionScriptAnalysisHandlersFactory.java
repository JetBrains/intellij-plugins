// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.JSAnalysisHandlersFactory;
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor;
import com.intellij.lang.javascript.validation.JSFunctionSignatureChecker;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.JSReferenceChecker;
import com.intellij.lang.javascript.validation.JSTypeChecker;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptAnalysisHandlersFactory extends JSAnalysisHandlersFactory {
  @Override
  public @NotNull JSAnnotatingVisitor createAnnotatingVisitor(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    return new ActionScriptAnnotatingVisitor(psiElement, holder);
  }

  @Override
  public @NotNull JSReferenceChecker getReferenceChecker(@NotNull JSProblemReporter<?> reporter) {
    return new ActionScriptReferenceChecker(reporter);
  }

  @Override
  public @NotNull <T> JSTypeChecker getTypeChecker(@NotNull JSProblemReporter<T> problemReporter) {
    return new ActionScriptTypeChecker(problemReporter);
  }

  @Override
  public @NotNull JSFunctionSignatureChecker getFunctionSignatureChecker(@NotNull ProblemsHolder holder, @NotNull JSTypeChecker typeChecker) {
    return new ActionScriptFunctionSignatureChecker(typeChecker);
  }
}
