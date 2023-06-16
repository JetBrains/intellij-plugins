// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsImportStatement;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public final class DroolsImportedStaticMembersProcessor implements DroolsDeclarationsProcessor {
  private static DroolsImportedStaticMembersProcessor myInstance;

  private DroolsImportedStaticMembersProcessor() {
  }

  public static DroolsImportedStaticMembersProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsImportedStaticMembersProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    for (PsiField psiField : getImportedStaticMembers(droolsFile)) {
      if (!processor.execute(psiField, state)) return false;
    }
    return true;
  }

  @NotNull
  public static Set<PsiField> getImportedStaticMembers(@NotNull DroolsFile droolsFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(droolsFile);
    final GlobalSearchScope scope =
      module != null ? module.getModuleRuntimeScope(false) : GlobalSearchScope.allScope(droolsFile.getProject());
    for (DroolsImportStatement anImport : Arrays.stream(droolsFile.getImports()).filter(anImport -> anImport.isStatic()).toList()) {
      final String imported = anImport.getImportQualifier().getText();
      if (StringUtil.isNotEmpty(imported)) {
        final String className = imported.substring(0, imported.lastIndexOf("."));
        final String memberName = StringUtil.getShortName(imported);
        if (StringUtil.isNotEmpty(memberName) && StringUtil.isNotEmpty(className)) {
          PsiClass psiClass = JavaPsiFacade.getInstance(droolsFile.getProject()).findClass(className, scope);
          if (psiClass != null) {
            final PsiField fieldByName = psiClass.findFieldByName(memberName, true);
            if (fieldByName != null) return Collections.singleton(fieldByName);
          }
        }
      }
    }
    return Collections.emptySet();
  }
}
