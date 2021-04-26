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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.protobuf.lang.psi.PbNumberValue;
import com.intellij.protobuf.lang.psi.ProtoNumberValue;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

abstract class PbNumberValueMixin extends PbElementBase implements PbNumberValue {

  private static final TokenSet NUMBER_TOKENS =
      TokenSet.create(
          ProtoTokenTypes.INTEGER_LITERAL,
          ProtoTokenTypes.FLOAT_LITERAL,
          ProtoTokenTypes.IDENTIFIER_LITERAL);

  PbNumberValueMixin(ASTNode node) {
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
      } else if (numberType == ProtoTokenTypes.IDENTIFIER_LITERAL) {
        if (isInf(childNode)) {
          return ProtoNumberValue.SourceType.INF;
        } else if (isNan(childNode)) {
          return ProtoNumberValue.SourceType.NAN;
        }
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

  private boolean isInf(ASTNode node) {
    return Objects.equals(node.getElementType(), ProtoTokenTypes.IDENTIFIER_LITERAL)
        && "inf".equals(node.getText());
  }

  private boolean isNan(ASTNode node) {
    return Objects.equals(node.getElementType(), ProtoTokenTypes.IDENTIFIER_LITERAL)
        && "nan".equals(node.getText());
  }
}
