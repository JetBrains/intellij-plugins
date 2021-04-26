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
package com.intellij.protobuf.lang.resolve.directive;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiReference;
import com.intellij.protobuf.lang.resolve.PbImportReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class FileComment extends SchemaComment {

  FileComment(@NotNull PsiComment comment, TextRange keyRange, TextRange nameRange, Type type) {
    super(comment, keyRange, nameRange, type);
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    String filename = getName();
    if (filename == null) {
      return null;
    }
    return new PbImportReference(filename, getComment(), getNameRange());
  }

  @Override
  public List<PsiReference> getAllReferences() {
    PsiReference ref = getReference();
    return ref != null ? ImmutableList.of(ref) : ImmutableList.of();
  }
}
