/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
