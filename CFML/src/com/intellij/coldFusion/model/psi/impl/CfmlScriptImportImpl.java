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
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public class CfmlScriptImportImpl extends CfmlCompositeElement implements CfmlImport {
  public CfmlScriptImportImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isImported(String componentName) {
    String importString = getImportString();
    if (importString == null) return false;
    if (importString.endsWith(componentName)) return true;
    if (importString.endsWith("*")) {
      PsiReference[] references = getReferences();
      for (PsiReference reference : references) {
        if (!(reference instanceof CfmlComponentReference)) {
          continue;
        }
        ResolveResult[] results = ((CfmlComponentReference)reference).multiResolve(false);
        for (ResolveResult result : results) {
          PsiElement element = result.getElement();
          if (element instanceof CfmlComponent) {
            if (Objects.equals(((CfmlComponent)element).getName(), componentName)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  @Nullable
  public String getImportString() {
    CfmlComponentReference referenceChild = findChildByType(CfmlElementTypes.COMPONENT_REFERENCE);
    if (referenceChild != null) {
      return referenceChild.getText();
    }
    return null;
  }

  @Override
  public String getPrefix() {
    return null;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    PsiElement reference = findChildByType(CfmlElementTypes.COMPONENT_REFERENCE);
    if (reference != null) {
      return new PsiReference[]{new CfmlComponentReference(reference.getNode(), this)};
    }
    return super.getReferences();
  }
}
