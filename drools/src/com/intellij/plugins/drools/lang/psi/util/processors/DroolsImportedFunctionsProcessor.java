// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsImport;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class DroolsImportedFunctionsProcessor implements DroolsDeclarationsProcessor {
  private static DroolsImportedFunctionsProcessor myInstance;

  private DroolsImportedFunctionsProcessor() {
  }

  public static DroolsImportedFunctionsProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsImportedFunctionsProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    for (PsiMethod importedFunction : getImportedFunctions(droolsFile)) {
      if (!processor.execute(importedFunction, state)) return false;
    }
    return true;
  }

  public static PsiMethod[] getImportedFunctions(@NotNull DroolsFile droolsFile) {
    final GlobalSearchScope scope = DroolsResolveUtil.getSearchScope(droolsFile);
    for (DroolsImport anImport : Arrays.stream(droolsFile.getImports()).filter(anImport -> anImport.isFunction()).toList()) {
      final String importedFunction = anImport.getImportedFunction();
      if (StringUtil.isNotEmpty(importedFunction)) {
        final String className = importedFunction.substring(0, importedFunction.lastIndexOf("."));
        final String methodName = StringUtil.getShortName(importedFunction);
        if (StringUtil.isNotEmpty(className) && StringUtil.isNotEmpty(methodName)) {
          PsiClass psiClass = JavaPsiFacade.getInstance(droolsFile.getProject()).findClass(className, scope);
          if (psiClass != null) {
            return psiClass.findMethodsByName(methodName, true);
          }
        }
      }
    }
    return PsiMethod.EMPTY_ARRAY;
  }
}
