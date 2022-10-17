package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public interface DroolsDeclarationsProcessor {
  /**
   * @return false to stop processing.
   */
  boolean processElement(@NotNull PsiScopeProcessor processor,
                         @NotNull ResolveState state,
                         PsiElement lastParent,
                         @NotNull PsiElement place, @NotNull DroolsFile droolsFile);
}
