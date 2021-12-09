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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.resolve.PbEnumValueReference;
import org.jetbrains.annotations.Nullable;

abstract class PbTextIdentifierValueMixin extends PbTextElementBase
    implements PbTextIdentifierValue {

  PbTextIdentifierValueMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    PbTextField field = PsiTreeUtil.getParentOfType(this, PbTextField.class);
    if (field == null) {
      return null;
    }
    PbNamedTypeElement namedTypeElement = field.getFieldName().getDeclaredNamedType();
    if (namedTypeElement instanceof PbEnumDefinition) {
      return new PbEnumValueReference(this, (PbEnumDefinition) namedTypeElement);
    }
    return null;
  }

  @Nullable
  @Override
  public Boolean getBooleanValue() {
    String text = getText();
    if ("true".equals(text) || "True".equals(text) || "t".equals(text)) {
      return Boolean.TRUE;
    } else if ("false".equals(text) || "False".equals(text) || "f".equals(text)) {
      return Boolean.FALSE;
    }
    return null;
  }

  @Nullable
  @Override
  public ProtoNumberValue getAsNumber() {
    PbTextNumberValue numberValue = new PbTextNumberValueImpl(getNode());
    if (numberValue.getSourceType() != null) {
      return numberValue;
    }
    // It wasn't a number.
    return null;
  }

  @Nullable
  @Override
  public Object getValue() {
    return getText();
  }
}
