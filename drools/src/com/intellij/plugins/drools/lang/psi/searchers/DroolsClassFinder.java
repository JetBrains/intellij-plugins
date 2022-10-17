// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugins.drools.lang.psi.DroolsDeclareStatement;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsTypeDeclaration;
import com.intellij.plugins.drools.lang.psi.indexes.DroolsDeclareStatementScalarIndex;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DroolsClassFinder extends PsiElementFinder {

  @Override
  @Nullable
  public PsiClass findClass(@NotNull String qualifiedName, @NotNull GlobalSearchScope scope) {
    if (scope.getProject() == null) return null;

    String packageName = StringUtil.getPackageName(qualifiedName);
    Collection<VirtualFile> filesByExt = ReadAction.compute(
      () -> FileBasedIndex.getInstance().getContainingFiles(DroolsDeclareStatementScalarIndex.Companion.getId(), packageName, scope));
    if (filesByExt.isEmpty()) return null;
    PsiManager psiManager = PsiManager.getInstance(scope.getProject());
    for (VirtualFile file : filesByExt) {
      PsiFile psiFile = psiManager.findFile(file);
      if (psiFile instanceof DroolsFile) {
        for (DroolsDeclareStatement declareStatement : ((DroolsFile)psiFile).getDeclarations()) {
          DroolsTypeDeclaration typeDeclaration = declareStatement.getTypeDeclaration();
          if (typeDeclaration != null && qualifiedName.equals(typeDeclaration.getQualifiedName())) {
            return new DroolsLightClass(typeDeclaration);
          }
        }
      }
    }
    return null;
  }

  @Override
  public PsiClass @NotNull [] findClasses(@NotNull String qualifiedName, @NotNull GlobalSearchScope scope) {
    PsiClass aClass = findClass(qualifiedName, scope);
    return aClass == null ? PsiClass.EMPTY_ARRAY : new PsiClass[]{aClass};
  }
}
