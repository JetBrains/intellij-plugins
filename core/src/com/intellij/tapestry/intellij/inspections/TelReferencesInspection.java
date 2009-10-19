/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.intellij.inspections;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.tapestry.TapestryBundle;
import com.intellij.tapestry.psi.TelQualifiedReference;
import com.intellij.tapestry.psi.TelReferenceExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TelReferencesInspection extends TapestryInspectionBase {

  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (!(element instanceof TelReferenceExpression)) return;
    TelQualifiedReference ref = ((TelReferenceExpression)element).getReference();
    if (!ref.isQualifierResolved()) return;
    final ResolveResult[] results = ref.multiResolve(false);
    final boolean resolvedWithError = results.length > 0 && !results[0].isValidResult();

    if (resolvedWithError || ref.resolve() == null) {
      final String message = ref.getUnresolvedMessage(resolvedWithError);
      holder.registerProblem(ref, message, resolvedWithError ? GENERIC_ERROR_OR_WARNING : LIKE_UNKNOWN_SYMBOL);
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return TapestryBundle.message("tel.references.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "TelReferencesInspection";
  }
}