/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
package com.intellij.coldFusion.navigation.actions;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.navigation.actions.TypeDeclarationPlaceAwareProvider;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CfmlTypeDeclarationProvider implements TypeDeclarationPlaceAwareProvider {
  @Nullable
  @Override
  public PsiElement[] getSymbolTypeDeclarations(@NotNull PsiElement targetElement, Editor editor, int offset) {
    PsiType type = null;
    if (targetElement instanceof CfmlVariable) {
      type = ((CfmlVariable)targetElement).getPsiType();
    } else if (targetElement instanceof CfmlFunction) {
      type = ((CfmlFunction)targetElement).getReturnType();
    }
    if (type == null) return null;
    if (type instanceof PsiArrayType) {
      type = ((PsiArrayType)type).getComponentType();
    }
    if (type instanceof CfmlComponentType) {
      Collection<CfmlComponent> resolveResults = ((CfmlComponentType)type).resolve();
      return resolveResults.size() > 0 ? resolveResults.toArray(new PsiElement[0]) : null;
    }
    return null;
  }

  @Nullable
  @Override
  public PsiElement[] getSymbolTypeDeclarations(@NotNull PsiElement symbol) {
    return getSymbolTypeDeclarations(symbol, null, -1);
  }
}
