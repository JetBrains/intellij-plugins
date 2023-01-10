// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsDeclareStatement;
import com.intellij.plugins.drools.lang.psi.DroolsEnumDeclaration;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsTypeDeclaration;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public final class DroolsDeclaredTypesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsDeclaredTypesProcessor myInstance;

  private DroolsDeclaredTypesProcessor() {
  }

  public static DroolsDeclaredTypesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsDeclaredTypesProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    DroolsDeclareStatement[] declarations = droolsFile.getDeclarations();
    for (DroolsDeclareStatement declaration : declarations) {
      if (!processTypeDeclaration(processor, state, declaration)) return false;
      if (!processEnumDeclaration(processor, state, declaration)) return false;
    }
    return true;
  }

  private static boolean processTypeDeclaration(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, DroolsDeclareStatement declaration) {
    DroolsTypeDeclaration typeDeclaration = declaration.getTypeDeclaration();
    if (typeDeclaration != null  && !processor.execute(new DroolsLightClass(typeDeclaration), state)) {
      return false;
    }
    return true;
  }

  private static boolean processEnumDeclaration(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, DroolsDeclareStatement declaration) {
    DroolsEnumDeclaration enumDeclaration = declaration.getEnumDeclaration();
    if (enumDeclaration != null  && !processor.execute(new DroolsLightClass(enumDeclaration), state)) {
      return false;
    }
    return true;
  }
}
