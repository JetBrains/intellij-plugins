// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.plugins.drools.lang.psi.util.processors.DroolsLhsBindVariablesProcessor.findRuleById;

public final class DroolsLhsOOPathBindVariablesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsLhsOOPathBindVariablesProcessor myInstance;

  private DroolsLhsOOPathBindVariablesProcessor() {
  }

  public static DroolsLhsOOPathBindVariablesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsLhsOOPathBindVariablesProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull final DroolsFile droolsFile) {

    final Set<PsiVariable> ooPathBinds = getOOPathBinds(place);
    for (PsiVariable bind : ooPathBinds) {
      if (!processor.execute(bind, state)) return false;
    }
    return true;
  }

  public static Set<PsiVariable> getOOPathBinds(@NotNull final PsiElement psiElement) {
    final PsiFile file = psiElement.getContainingFile();
    return file instanceof DroolsFile
           ? getOOPathBinds(psiElement, (DroolsFile)file, PsiTreeUtil.getParentOfType(psiElement, DroolsRuleStatement.class))
           : Collections.emptySet();
  }

  private static Set<PsiVariable> getOOPathBinds(@NotNull final PsiElement psiElement,
                                                 @NotNull final DroolsFile droolsFile,
                                                 @Nullable final DroolsRuleStatement droolsRule) {
    return getOOPathBinds(psiElement, droolsFile, droolsRule, new HashSet<>());
  }

  private static Set<PsiVariable> getOOPathBinds(@NotNull final PsiElement psiElement,
                                                 @NotNull final DroolsFile droolsFile,
                                                 @Nullable final DroolsRuleStatement droolsRule,
                                                 Set<DroolsRuleStatement> visited) {
    if (droolsRule == null) return Collections.emptySet();
    visited.add(droolsRule);

    final Set<PsiVariable> binds = getVariables(psiElement, droolsRule);

    final DroolsParentRule parentRuleRef = droolsRule.getParentRule();
    if (parentRuleRef != null) {
      final DroolsRuleStatement parentRule = findRuleById(parentRuleRef.getStringId(), droolsFile);
      if (parentRule != null && !visited.contains(parentRule)) {
        binds.addAll(getOOPathBinds(psiElement, droolsFile, parentRule, visited));
      }
    }

    return binds;
  }

  private static Set<PsiVariable> getVariables(final PsiElement psiElement, DroolsRuleStatement droolsRule) {
    final Set<PsiVariable> binds = new HashSet<>();

    droolsRule.acceptChildren(new DroolsVisitor() {
      @Override
      public void visitLhsOOPathBind(@NotNull DroolsLhsOOPathBind lhsOOPathBind) {
        DroolsNameId bindIdentifier = lhsOOPathBind.getNameId();
        if (bindIdentifier != null) {
          String id = bindIdentifier.getText();
          if (id != null) {
            binds.add(lhsOOPathBind);
          }
        }
        lhsOOPathBind.acceptChildren(this);
      }

      @Override
      public void visitLhsPatternBind(@NotNull DroolsLhsPatternBind patternBind) {
        DroolsNameId bindIdentifier = patternBind.getNameId();
        if (bindIdentifier != null) {
          String id = bindIdentifier.getText();
          if (id != null) {
            binds.add(patternBind);
          }
        }
        patternBind.acceptChildren(this);
      }

      @Override
      public void visitPsiCompositeElement(@NotNull DroolsPsiCompositeElement o) {
        o.acceptChildren(this);
      }
    });
    return binds;
  }
}
