// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.util.processors.DroolsImportedClassesProcessor;
import com.intellij.plugins.drools.lang.psi.util.processors.DroolsImportedPackagesProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class DroolsCompositeBlockExpressionElement extends ASTWrapperPsiElement {
  public DroolsCompositeBlockExpressionElement(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {

    DroolsFile droolsFile = PsiTreeUtil.getContextOfType(place, DroolsFile.class);
    if (droolsFile != null) {
      if (!DroolsImportedPackagesProcessor.getInstance().processElement(processor, state, lastParent, place, droolsFile)) return false;
      if (!DroolsImportedClassesProcessor.getInstance().processElement(processor, state, lastParent, place, droolsFile)) return false;
      final DroolsFunctionStatement functionStatement = PsiTreeUtil.getParentOfType(place, DroolsFunctionStatement.class);
      if (functionStatement != null) {
        for (PsiParameter psiParameter : functionStatement.getParameterList().getParameters()) {
          if (!processor.execute(psiParameter, state)) return false;
        }
      }
    }
    return super.processDeclarations(processor, state, lastParent, place);
  }
}
