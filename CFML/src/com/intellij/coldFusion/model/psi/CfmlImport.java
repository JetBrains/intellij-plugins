/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public interface CfmlImport extends PsiElement {
  default boolean isImported(String componentName) {
    String importString = getComponentQualifiedName(componentName);
    return importString != null;
  };

  @Nullable
  String getImportString();
  
  @Nullable
  default String getComponentQualifiedName(String componentName) {
    String importString = getImportString();
    if (importString == null) return null;
    String[] importedPathParts = importString.split("\\.");
    String importedName = importedPathParts[importedPathParts.length - 1];
    if (importedName.equals(componentName)) return importString;
    if (importedName.equals("*")) {
      PsiReference[] references = getReferences();
      for (PsiReference reference : references) {
        if (!(reference instanceof CfmlComponentReference)) {
          continue;
        }
        ResolveResult[] results = ((CfmlComponentReference)reference).multiResolve(false);
        for (ResolveResult result : results) {
          PsiElement element = result.getElement();
          if (element instanceof CfmlComponent) {
            CfmlComponent component = (CfmlComponent)element;
            if (Objects.equals(component.getName(), componentName)) {
              String packagePath = String.join(".", Arrays.copyOfRange(importedPathParts, 0, importedPathParts.length - 1));
              return packagePath + "." + component.getName();
            }
          }
        }
      }
    }
    return null;
  };
  
  default Set<String> getAllComponentQualifiedNames() {
    String importString = getImportString();
    if (importString == null) return null;
    String[] importedPathParts = importString.split("\\.");
    String importedName = importedPathParts[importedPathParts.length - 1];
    Set<String> set = new HashSet<String>();
    if (!importedName.equals("*")) {
      set.add(importString);
    } else {
      PsiReference[] references = getReferences();
      for (PsiReference reference : references) {
        if (!(reference instanceof CfmlComponentReference)) {
          continue;
        }
        ResolveResult[] results = ((CfmlComponentReference)reference).multiResolve(false);
        for (ResolveResult result : results) {
          PsiElement element = result.getElement();
          if (element instanceof CfmlComponent) {
            CfmlComponent component = (CfmlComponent)element;
            String packagePath = String.join(".", Arrays.copyOfRange(importedPathParts, 0, importedPathParts.length - 1));
            set.add(packagePath + "." + component.getName());
          }
        }
      }
    }
    return set;
  }

  @Nullable
  String getPrefix();
}
