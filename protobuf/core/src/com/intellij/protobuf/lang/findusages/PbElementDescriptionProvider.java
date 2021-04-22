/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.findusages;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.usageView.UsageViewLongNameLocation;
import com.intellij.protobuf.lang.psi.PbSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides the title for the find usages tool window. */
public class PbElementDescriptionProvider implements ElementDescriptionProvider {

  @Nullable
  @Override
  public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
    if (location instanceof UsageViewLongNameLocation) {
      if (element instanceof PbSymbol) {
        PbSymbol symbol = (PbSymbol) element;
        QualifiedName qualifiedName = symbol.getQualifiedName();
        if (qualifiedName != null) {
          return qualifiedName.toString();
        }
        return symbol.getName();
      }
    }
    return null;
  }
}
