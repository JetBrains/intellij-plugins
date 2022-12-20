package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectOptionHolder;
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
  public JSKeywordHighlighterVisitor createKeywordHighlighterVisitor(@NotNull HighlightInfoHolder holder,
                                                                     @NotNull DialectOptionHolder dialectOptionHolder) {
    return new ActionScriptKeywordHighlighterVisitor(holder);
  }

  @NotNull
  @Override
  public JSReferenceChecker getReferenceChecker(@NotNull JSProblemReporter<?> reporter) {
    return new ActionScriptReferenceChecker(reporter);
  }

  @NotNull
  @Override
  public JSTypeChecker getTypeChecker(ProblemsHolder holder) {
    return new ActionScriptTypeChecker(getProblemReporter(holder));
  }

  @NotNull
  @Override
  public JSFunctionSignatureChecker getFunctionSignatureChecker(@NotNull ProblemsHolder holder, @NotNull JSTypeChecker typeChecker) {
    return new ActionScriptFunctionSignatureChecker(typeChecker);
  }
}
