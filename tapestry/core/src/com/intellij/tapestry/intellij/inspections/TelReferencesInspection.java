/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.intellij.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.tapestry.TapestryBundle;
import com.intellij.tapestry.psi.TelQualifiedReference;
import com.intellij.tapestry.psi.TelReferenceExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;

/**
 * @author Alexey Chmutov
 */
public class TelReferencesInspection extends TapestryInspectionBase {

  @Override
  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (!(element instanceof TelReferenceExpression)) return;
    TelQualifiedReference ref = ((TelReferenceExpression)element).getReference();
    if (!ref.isQualifierResolved()) return;
    final ResolveResult[] results = ref.multiResolve(false);
    final boolean resolvedWithError = results.length > 0 && !results[0].isValidResult();

    if (resolvedWithError || results.length == 0) { // can not check ref.resolve() for null here as we can have 2 results
      final String message = ref.getUnresolvedMessage(resolvedWithError);
      holder.registerProblem(ref, message, resolvedWithError ? GENERIC_ERROR_OR_WARNING : LIKE_UNKNOWN_SYMBOL);
    }
  }

  @Override
  @NonNls
  @NotNull
  public String getShortName() {
    return "TelReferencesInspection";
  }
}