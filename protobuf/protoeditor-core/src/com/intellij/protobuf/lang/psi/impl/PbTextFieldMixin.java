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
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class PbTextFieldMixin extends PbTextElementBase implements PbTextField {

  PbTextFieldMixin(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public List<PbTextElement> getValues() {
    PbTextValueList valueList = getValueList();
    PsiElement parent = valueList != null ? valueList : this;
    return Stream.of(parent.getChildren())
        .filter(c -> c instanceof PbTextLiteral || c instanceof PbTextMessage)
        .map(c -> (PbTextElement) c)
        .collect(Collectors.toList());
  }
}
