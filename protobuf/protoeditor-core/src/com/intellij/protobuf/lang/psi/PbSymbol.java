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

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.Nullable;

/**
 * A symbol is a named entity in a proto file that contributes to fully-qualified names. Many
 * symbols are also {@link PbStatement}s, but not all. For example, a {@link PbPackageName} is a
 * symbol, but not a statement (the {@link PbPackageStatement} contains the package name).
 */
public interface PbSymbol extends PbElement, PsiNamedElement, PsiNameIdentifierOwner {

  /** Returns the fully-qualified name of this element as a string. */
  @Nullable
  QualifiedName getQualifiedName();

  /** Returns the {@link PbSymbolOwner} ancestor of this symbol. */
  @Nullable
  PbSymbolOwner getSymbolOwner();
}
