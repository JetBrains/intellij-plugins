package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public final class DroolsGlobalVariablesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsGlobalVariablesProcessor myInstance;

  private DroolsGlobalVariablesProcessor() {
  }

  public static DroolsGlobalVariablesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsGlobalVariablesProcessor();
    }
    return myInstance;
  }
  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {

    for (PsiVariable psiVariable : droolsFile.getGlobalVariables()) {
      if (!processor.execute(psiVariable, state)) return false;
    }
    return true;
  }
}
