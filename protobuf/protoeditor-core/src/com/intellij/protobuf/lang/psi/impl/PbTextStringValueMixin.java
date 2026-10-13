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
import com.intellij.protobuf.lang.psi.PbTextStringPart;
import com.intellij.protobuf.lang.psi.PbTextStringValue;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PbTextStringValueMixin extends PbTextElementBase
    implements PbTextStringValue {

  public PbTextStringValueMixin(ASTNode node) {
    super(node);
  }

  @Override
  public boolean isValidHost() {
    List<PbTextStringPart> parts = getStringParts();
    if (parts.isEmpty()) {
      return false;
    }
    for (PbTextStringPart part : parts) {
      if (part.isUnterminated()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @Override
  public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return new PbTextStringValueEscaper(this);
  }
}
