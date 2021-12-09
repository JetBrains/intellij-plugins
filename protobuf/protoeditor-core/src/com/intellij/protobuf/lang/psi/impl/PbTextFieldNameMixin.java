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
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.resolve.PbTextFieldNameReference;
import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.Nullable;

abstract class PbTextFieldNameMixin extends PbTextElementBase implements PbTextFieldName {

  PbTextFieldNameMixin(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public PbField getDeclaredField() {
    PsiReference ref = getEffectiveReference();
    if (ref == null) {
      return null;
    }
    PsiElement resolved = ref.resolve();
    if (!(resolved instanceof PbField)) {
      return null;
    }
    return (PbField) resolved;
  }

  @Nullable
  @Override
  public PbNamedTypeElement getDeclaredNamedType() {
    PsiReference ref = null;
    PbTextExtensionName extensionName = getExtensionName();
    if (extensionName != null && extensionName.isAnyTypeUrl()) {
      // This is an any type. The extension name itself refers to the declared type.
      ref = extensionName.getEffectiveReference();
    } else {
      // This is an extension field name. The declared type is the field's type.
      PbField field = getDeclaredField();
      if (field != null) {
        PbTypeName typeName = field.getTypeName();
        if (typeName != null) {
          ref = typeName.getEffectiveReference();
        }
      }
    }

    if (ref == null) {
      return null;
    }
    PsiElement refElement = ref.resolve();
    if (!(refElement instanceof PbNamedTypeElement)) {
      return null;
    }
    return (PbNamedTypeElement) refElement;
  }

  @Nullable
  @Override
  public BuiltInType getDeclaredBuiltInType() {
    PbField field = getDeclaredField();
    if (field == null) {
      return null;
    }
    PbTypeName typeName = field.getTypeName();
    if (typeName == null) {
      return null;
    }
    return typeName.getBuiltInType();
  }

  @Override
  public PsiReference getReference() {
    if (getExtensionName() != null) {
      // Extension name references are handled by the ExtensionName child.
      return null;
    } else {
      return new PbTextFieldNameReference(this);
    }
  }

  @Nullable
  @Override
  public PsiReference getEffectiveReference() {
    PbTextExtensionName extensionName = getExtensionName();
    if (extensionName != null) {
      if (extensionName.isAnyTypeUrl()) {
        // Any type url. The symbol path refers to a message type, not a field.
        return null;
      } else {
        return extensionName.getEffectiveReference();
      }
    } else {
      return new PbTextFieldNameReference(this);
    }
  }
}
