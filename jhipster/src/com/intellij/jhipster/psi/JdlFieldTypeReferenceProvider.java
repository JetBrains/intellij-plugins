// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JdlFieldTypeReferenceProvider {
  ExtensionPointName<JdlFieldTypeReferenceProvider> EP_NAME = 
      ExtensionPointName.create("com.intellij.jhipster.fieldTypeReferenceProvider");
  
  /**
   * Creates a reference for the given JdlFieldType.
   * 
   * @param fieldType The field type element to create a reference for
   * @return A PsiReference for the field type, or null if no reference can be created
   */
  @Nullable
  PsiReference createReference(@NotNull JdlFieldType fieldType);

  @Nullable static JdlFieldTypeReferenceProvider getInstance() {
    var extensions = EP_NAME.getExtensionList();
    return extensions.isEmpty() ? null : extensions.get(0);
  }
}