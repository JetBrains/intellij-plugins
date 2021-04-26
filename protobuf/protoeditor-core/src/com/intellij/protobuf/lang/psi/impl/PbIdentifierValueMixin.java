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
import com.intellij.protobuf.lang.resolve.PbEnumValueReference;
import org.jetbrains.annotations.Nullable;

abstract class PbIdentifierValueMixin extends PbElementBase implements PbIdentifierValue {

  PbIdentifierValueMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    PsiElement parent = getParent();
    if (parent instanceof PbOptionExpression) {
      PbOptionName optionName = ((PbOptionExpression) parent).getOptionName();
      PbNamedTypeElement namedTypeElement = optionName.getNamedType();
      if (namedTypeElement instanceof PbEnumDefinition) {
        return new PbEnumValueReference(this, (PbEnumDefinition) namedTypeElement);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Boolean getBooleanValue() {
    String text = getText();
    if ("true".equals(text)) {
      return Boolean.TRUE;
    } else if ("false".equals(text)) {
      return Boolean.FALSE;
    }
    return null;
  }

  @Nullable
  @Override
  public PbNumberValue getAsNumber() {
    PbNumberValue numberValue = new PbNumberValueImpl(getNode());
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
