package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.JSAnalysisHandlersFactory;
import com.intellij.lang.javascript.validation.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptAnalysisHandlersFactory extends JSAnalysisHandlersFactory {
  @NotNull
  @Override
  public JSAnnotatingVisitor createAnnotatingVisitor(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    return new ActionScriptAnnotatingVisitor(psiElement, holder);
  }

  @Override
  public JSKeywordHighlighterVisitor createKeywordHighlighterVisitor(@NotNull AnnotationHolder holder) {
    return new ActionScriptKeywordHighlighterVisitor(holder);
  }

  @NotNull
  @Override
  public JSReferenceChecker getReferenceChecker(@NotNull JSReferenceInspectionProblemReporter reporter) {
    return new ActionScriptReferenceChecker(reporter);
  }
}
