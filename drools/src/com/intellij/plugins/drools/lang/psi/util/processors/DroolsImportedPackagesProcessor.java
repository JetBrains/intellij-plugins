// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public final class DroolsImportedPackagesProcessor implements DroolsDeclarationsProcessor {

  private static DroolsImportedPackagesProcessor myInstance;

  private DroolsImportedPackagesProcessor() {
  }

  public static DroolsImportedPackagesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsImportedPackagesProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    for (PsiPackage psiPackage : DroolsResolveUtil.getDefaultPackages(droolsFile)) {
      if (psiPackage != null) {
        if (!psiPackage.processDeclarations(processor, state, lastParent, place)) return false;
      }
    }
    for (PsiPackage psiPackage : DroolsResolveUtil.getExplicitlyImportedPackages(droolsFile)) {
      if (psiPackage != null) {
        if (!psiPackage.processDeclarations(processor, state, lastParent, place)) return false;
        for (PsiClass aClass : psiPackage.getClasses()) {
          if (!aClass.processDeclarations(processor, state, lastParent, place)) return false;
        }
      }
    }
    return true;
  }
}
