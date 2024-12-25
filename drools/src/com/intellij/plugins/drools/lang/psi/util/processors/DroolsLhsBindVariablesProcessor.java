// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightVariable;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DroolsLhsBindVariablesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsLhsBindVariablesProcessor myInstance;
  private static final List<String> ACCUMULATE_INT_FUNCTIONS = Arrays.asList("sum", "average", "min", "max", "count");

  private DroolsLhsBindVariablesProcessor() {
  }

  public static DroolsLhsBindVariablesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsLhsBindVariablesProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, final @NotNull DroolsFile droolsFile) {

    final Set<PsiVariable> binds = getPatternBinds(place);
    for (PsiVariable bind : binds) {
      if (!processor.execute(bind, state)) return false;
    }
    return true;
  }

  public static Set<PsiVariable> getPatternBinds(final @NotNull PsiElement psiElement) {
    final PsiFile file = psiElement.getContainingFile();
    return file instanceof DroolsFile
           ? getPatternBinds(psiElement, (DroolsFile)file, PsiTreeUtil.getParentOfType(psiElement, DroolsRuleStatement.class))
           : Collections.emptySet();
  }

  private static Set<PsiVariable> getPatternBinds(final @NotNull PsiElement psiElement,
                                                  final @NotNull DroolsFile droolsFile,
                                                  final @Nullable DroolsRuleStatement droolsRule) {
    return getPatternBinds(psiElement, droolsFile, droolsRule, new HashSet<>());
  }

  private static Set<PsiVariable> getPatternBinds(final @NotNull PsiElement psiElement,
                                                  final @NotNull DroolsFile droolsFile,
                                                  final @Nullable DroolsRuleStatement droolsRule,
                                                  Set<DroolsRuleStatement> visited) {
    if (droolsRule == null) return Collections.emptySet();
    visited.add(droolsRule);

    final Set<PsiVariable> binds = getVariables(psiElement, droolsRule);

    final DroolsParentRule parentRuleRef = droolsRule.getParentRule();
    if (parentRuleRef != null) {
      final DroolsRuleStatement parentRule = findRuleById(parentRuleRef.getStringId(), droolsFile);
      if (parentRule != null && !visited.contains(parentRule)) {
        binds.addAll(getPatternBinds(psiElement, droolsFile, parentRule, visited));
      }
    }

    return binds;
  }

  private static Set<PsiVariable> getVariables(final PsiElement psiElement, DroolsRuleStatement droolsRule) {
    final Set<PsiVariable> binds = new HashSet<>();

    droolsRule.acceptChildren(new DroolsVisitor() {
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
      public void visitUnaryAssignExpr(@NotNull DroolsUnaryAssignExpr unaryAssignExpr) {
        // f1 :  Fibonacci( s1 : sequence, value != -1 )  --> process "s1: sequence"
        final DroolsNameId identifier = unaryAssignExpr.getNameId();
        String id = identifier.getText();
        if (id != null) {
          final DroolsUnaryAssignExpr parentUnaryAssignExpr = PsiTreeUtil.getParentOfType(psiElement, DroolsUnaryAssignExpr.class);
          if (!unaryAssignExpr.equals(parentUnaryAssignExpr)) {
            binds.add(unaryAssignExpr);
          }
        }
      }

      @Override
      public void visitAccumulateFunctionBinding(@NotNull DroolsAccumulateFunctionBinding functionBinding) {
        final DroolsNameId identifier = functionBinding.getNameId();
        String id = identifier.getText();
        if (id != null) {
          PsiType psiType =
            ACCUMULATE_INT_FUNCTIONS.contains(functionBinding.getAccumulateFunction().getFunctionName().getText())
            ? PsiTypes.intType()
            : PsiTypes.voidType();
          binds.add(new DroolsLightVariable(id, psiType, identifier.getNavigationElement()));
        }
      }

      @Override
      public void visitPsiCompositeElement(@NotNull DroolsPsiCompositeElement o) {
        o.acceptChildren(this);
      }
    });
    return binds;
  }

  public static @Nullable DroolsRuleStatement findRuleById(@Nullable DroolsStringId id, @NotNull DroolsFile droolsFile) {
    if (id != null) {
      final String ruleIdText = id.getText();
      if (!StringUtil.isEmpty(ruleIdText)) {
        for (DroolsRuleStatement ruleStatement : droolsFile.getRules()) {
          if (ruleIdText.equals(ruleStatement.getRuleName().getStringId().getText())) return ruleStatement;
        }
      }
    }
    return null;
  }
}
