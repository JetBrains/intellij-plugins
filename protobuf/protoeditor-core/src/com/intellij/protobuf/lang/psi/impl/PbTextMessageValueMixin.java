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
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbTextMessageValueMixin extends PbTextElementBase implements PbTextMessageValue {

  private static final TokenSet START_TOKENS =
      TokenSet.create(ProtoTokenTypes.LBRACE, ProtoTokenTypes.LT);
  private static final TokenSet END_TOKENS =
      TokenSet.create(ProtoTokenTypes.RBRACE, ProtoTokenTypes.GT);

  PbTextMessageValueMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PbMessageType getDeclaredMessage() {
    PbTextField parentField = PsiTreeUtil.getParentOfType(this, PbTextField.class);
    if (parentField == null) {
      return null;
    }
    PbNamedTypeElement namedType = parentField.getFieldName().getDeclaredNamedType();
    if (!(namedType instanceof PbMessageType)) {
      return null;
    }
    return (PbMessageType) namedType;
  }

  @NotNull
  @Override
  public PsiElement getStart() {
    return findNotNullChildByType(START_TOKENS);
  }

  @Nullable
  @Override
  public PsiElement getEnd() {
    return findChildByType(END_TOKENS);
  }
}
