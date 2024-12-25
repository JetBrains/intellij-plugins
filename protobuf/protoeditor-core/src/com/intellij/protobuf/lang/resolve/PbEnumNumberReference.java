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
package com.intellij.protobuf.lang.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.psi.PbEnumDefinition;
import com.intellij.protobuf.lang.psi.PbEnumValue;
import com.intellij.protobuf.lang.psi.PbNumberValue;
import com.intellij.protobuf.lang.psi.ProtoNumberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides a reference to a {@link PbEnumValue}. */
public class PbEnumNumberReference extends PsiReferenceBase<ProtoNumberValue> {

  private final @NotNull PbEnumDefinition enumDefinition;

  public PbEnumNumberReference(
      @NotNull ProtoNumberValue element, @NotNull PbEnumDefinition enumDefinition) {
    super(element);
    this.enumDefinition = enumDefinition;
  }

  @Override
  protected @Nullable TextRange calculateDefaultRangeInElement() {
    return TextRange.create(0, myElement.getTextLength());
  }

  @Override
  public @Nullable PsiElement resolve() {
    if (!myElement.isValidInt32()) {
      return null;
    }
    Long longValue = myElement.getLongValue();
    if (longValue == null) {
      return null;
    }
    return enumDefinition
        .getStatements()
        .stream()
        .filter(s -> s instanceof PbEnumValue)
        .map(s -> (PbEnumValue) s)
        .filter(
            v -> {
              PbNumberValue numberValue = v.getNumberValue();
              return numberValue != null && longValue.equals(numberValue.getLongValue());
            })
        .findFirst()
        .orElse(null);
  }

  @Override
  public @NotNull Object[] getVariants() {
    // We don't give suggestions for enum numbers, only named values
    // (handled by PbEnumValueReference).
    return LookupElement.EMPTY_ARRAY;
  }
}
