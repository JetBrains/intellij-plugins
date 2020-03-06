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
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

public interface CfmlTypedVariable extends CfmlVariable {

  @Nullable
  default PsiType getPsiType() {
    String typeString = getType();
    if (typeString == null) return null;
    CfmlFile file = getContainingFile();

    final boolean isArray = typeString.endsWith("[]");
    final String qualifiedTypeString;
    if (isArray) {
      qualifiedTypeString = file.getComponentQualifiedName(typeString.substring(0, typeString.length() - 2));
    } else {
      qualifiedTypeString = file.getComponentQualifiedName(typeString);
    }
    if (qualifiedTypeString == null) return null;
    if (isArray) {
      return new CfmlArrayType(qualifiedTypeString, file, getProject());
    } else {
      return new CfmlComponentType(qualifiedTypeString, file, getProject());
    }
  }

  @Override
  CfmlFile getContainingFile();

  @Nullable
  String getType();
}
