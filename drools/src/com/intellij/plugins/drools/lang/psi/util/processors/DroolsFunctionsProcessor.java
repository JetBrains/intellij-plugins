// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionLightMethodBuilder;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public final class DroolsFunctionsProcessor implements DroolsDeclarationsProcessor {
  private static DroolsFunctionsProcessor myInstance;

  private DroolsFunctionsProcessor() {
  }

  public static DroolsFunctionsProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsFunctionsProcessor();
    }
    return myInstance;
  }
  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    for (final DroolsFunctionStatement functionStatement : droolsFile.getFunctions()) {
      if (!processor.execute(functionStatement, state)) return false;
    }
    return true;
  }

  public static LightMethodBuilder createLightMethodBuilder(final @NotNull DroolsFunctionStatement function) {
    return new DroolsFunctionLightMethodBuilder(function);
  }
}
