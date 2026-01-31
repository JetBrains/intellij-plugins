// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

final class JdlFieldTypeReferenceProviderBackendImpl implements JdlFieldTypeReferenceProvider {
  @Override
  public @NotNull PsiReference createReference(@NotNull JdlFieldType fieldType) {
    return new JdlFieldTypeReference(fieldType);
  }
}