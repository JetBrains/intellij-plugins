// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.project.Project;
import com.intellij.plugins.drools.DroolsConstants;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightVariable;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DroolsImplicitVariablesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsImplicitVariablesProcessor myInstance;

  private static final Map<String, String> vars = new HashMap<>();

  static {
    vars.put("kcontext", DroolsConstants.KIE_CONTEXT_CLASS);
    vars.put("drools", DroolsConstants.KNOWLEDGE_HELPER_CLASS);
  }
  private DroolsImplicitVariablesProcessor() {
  }

  public static DroolsImplicitVariablesProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsImplicitVariablesProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull final DroolsFile droolsFile) {

    Set<DroolsLightVariable> implicitVars = CachedValuesManager.getCachedValue(droolsFile, () -> {
      Set<DroolsLightVariable> lightVariables = new HashSet<>();
      for (Map.Entry<String, String> entry : vars.entrySet()) {
        final String className = entry.getValue();
        final Project project = droolsFile.getProject();
        PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        if (aClass != null) {
          final String varName = entry.getKey();
          lightVariables.add(new DroolsLightVariable(varName, JavaPsiFacade.getInstance(project).getElementFactory().createType(new DroolsLightClass(aClass)), droolsFile));
        }
      }
      return CachedValueProvider.Result.createSingleDependency(lightVariables, droolsFile);
    });

    for (DroolsLightVariable lightVariable : implicitVars) {
      if (!processor.execute(lightVariable, state)) return false;

      if (lightVariable.getName().equals("drools")) {
        final PsiClass psiClass = ((PsiClassType)lightVariable.getType()).resolve();

        if (psiClass != null) {
          if (!psiClass.processDeclarations(processor,state,lastParent, place)) return false;
        }
      }
    }

    return true;
  }
}
