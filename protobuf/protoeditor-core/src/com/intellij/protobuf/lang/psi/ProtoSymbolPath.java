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
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A SymbolPath is a recursive structure of dot-delimited symbols, used as type names. See {@link
 * ProtoSymbolPathReference} for more
 * information on its structure.
 */
public interface ProtoSymbolPath extends PsiElement, PsiNamedElement, PsiNameIdentifierOwner {

  /** Return this path's {@link ProtoSymbolPathContainer}. */
  @Nullable
  default ProtoSymbolPathContainer getPathContainer() {
    return PsiTreeUtil.getParentOfType(this, ProtoSymbolPathContainer.class);
  }

  /** Return a {@link QualifiedName} containing the path's components. */
  @NotNull
  default QualifiedName getQualifiedName() {
    ProtoSymbolPath qualifier = getQualifier();
    if (qualifier != null) {
      return qualifier.getQualifiedName().append(getSymbol().getText());
    } else {
      return QualifiedName.fromComponents(getSymbol().getText());
    }
  }

  /** Return the qualifying {@link ProtoSymbolPath component}. */
  @Nullable
  default ProtoSymbolPath getQualifier() {
    return PsiTreeUtil.getChildOfType(this, ProtoSymbolPath.class);
  }

  /** Return the leaf element containing the actual symbol. */
  @NotNull
  PsiElement getSymbol();
}
