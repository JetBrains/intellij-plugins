// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.project.Project;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightVariable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.intellij.plugins.drools.DroolsConstants.KIE_CONTEXT_CLASS;
import static com.intellij.plugins.drools.DroolsConstants.KNOWLEDGE_HELPER_8_X;
import static com.intellij.plugins.drools.DroolsConstants.KNOWLEDGE_HELPER_CLASS;

public final class DroolsImplicitVariablesProcessor implements DroolsDeclarationsProcessor {
  private static DroolsImplicitVariablesProcessor myInstance;

  private static final Map<String, List<String>> vars = new HashMap<>();

  static {
    vars.put("kcontext", Collections.singletonList(KIE_CONTEXT_CLASS));
    vars.put("drools", Arrays.asList(KNOWLEDGE_HELPER_CLASS, KNOWLEDGE_HELPER_8_X));
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
                                @NotNull PsiElement place, final @NotNull DroolsFile droolsFile) {

    Set<DroolsLightVariable> implicitVars = CachedValuesManager.getCachedValue(droolsFile, () -> {
      Set<DroolsLightVariable> lightVariables = new HashSet<>();
      final Project project = droolsFile.getProject();
      for (Map.Entry<String, List<String>> entry : vars.entrySet()) {
        for (String className : entry.getValue()) {
          PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
          if (aClass != null) {
            final String varName = entry.getKey();
            lightVariables.add(new DroolsLightVariable(varName, JavaPsiFacade.getInstance(project).getElementFactory()
              .createType(new DroolsLightClass(aClass)), droolsFile));
            break;
          }
        }
      }
      return CachedValueProvider.Result.createSingleDependency(lightVariables, droolsFile);
    });

    for (DroolsLightVariable lightVariable : implicitVars) {
      if (!processor.execute(lightVariable, state)) return false;

      if (lightVariable.getName().equals("drools")) {
        final PsiClass psiClass = ((PsiClassType)lightVariable.getType()).resolve();

        if (psiClass != null) {
          if (!psiClass.processDeclarations(processor, state, lastParent, place)) return false;
        }
      }
    }

    return true;
  }
}
