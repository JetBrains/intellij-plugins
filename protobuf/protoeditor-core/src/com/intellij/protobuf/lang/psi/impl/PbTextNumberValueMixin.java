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

import com.google.common.base.Ascii;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.resolve.PbEnumNumberReference;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

abstract class PbTextNumberValueMixin extends PbTextElementBase implements PbTextNumberValue {

  private static final TokenSet NUMBER_TOKENS =
      TokenSet.create(
          ProtoTokenTypes.INTEGER_LITERAL,
          ProtoTokenTypes.FLOAT_LITERAL,
          ProtoTokenTypes.IDENTIFIER_LITERAL);

  PbTextNumberValueMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public ProtoNumberValue.SourceType getSourceType() {
    PsiElement childElement = getNumberElement();
    ASTNode childNode = childElement != null ? childElement.getNode() : null;
    if (childNode != null) {
      IElementType numberType = childNode.getElementType();
      if (numberType == ProtoTokenTypes.INTEGER_LITERAL) {
        return ProtoNumberValue.SourceType.INTEGER;
      } else if (numberType == ProtoTokenTypes.FLOAT_LITERAL) {
        return ProtoNumberValue.SourceType.FLOAT;
      } else if (isInf(childNode)) {
        return ProtoNumberValue.SourceType.INF;
      } else if (isNan(childNode)) {
        return ProtoNumberValue.SourceType.NAN;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public PsiElement getNumberElement() {
    return findChildByType(NUMBER_TOKENS);
  }

  @Override
  public boolean isNegative() {
    return findChildByType(ProtoTokenTypes.MINUS) != null;
  }

  @Override
  public Boolean getBooleanValue() {
    if (isNegative()) {
      return null;
    }
    Long longValue = getLongValue();
    if (longValue == null) {
      return null;
    } else if (longValue == 0) {
      return Boolean.FALSE;
    } else if (longValue == 1) {
      return Boolean.TRUE;
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    if (!isValidInt32()) {
      return null;
    }
    PbTextField field = PsiTreeUtil.getParentOfType(this, PbTextField.class);
    if (field == null) {
      return null;
    }
    PbNamedTypeElement namedTypeElement = field.getFieldName().getDeclaredNamedType();
    if (namedTypeElement instanceof PbEnumDefinition) {
      return new PbEnumNumberReference(this, (PbEnumDefinition) namedTypeElement);
    }
    return null;
  }

  private static boolean isInf(ASTNode node) {
    String text = Ascii.toLowerCase(node.getText());
    return Objects.equals(node.getElementType(), ProtoTokenTypes.IDENTIFIER_LITERAL)
        && ("inf".equals(text) || "infinity".equals(text));
  }

  private static boolean isNan(ASTNode node) {
    String text = Ascii.toLowerCase(node.getText());
    return Objects.equals(node.getElementType(), ProtoTokenTypes.IDENTIFIER_LITERAL)
        && "nan".equals(text);
  }
}
