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
import com.intellij.protobuf.lang.psi.PbImportStatement;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbImportStatementMixin extends PbStatementBase implements PbImportStatement {

  private static final TokenSet IMPORT_LABELS =
    TokenSet.create(ProtoTokenTypes.PUBLIC, ProtoTokenTypes.WEAK, ProtoTokenTypes.OPTION);

  PbImportStatementMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PsiElement getImportLabel() {
    ASTNode node = getNode().findChildByType(IMPORT_LABELS);
    return node == null ? null : node.getPsi();
  }

  private @Nullable PsiElement getImportLabelByType(@NotNull IElementType type) {
    ASTNode node = getNode().findChildByType(type);
    return node == null ? null : node.getPsi();
  }

  @Override
  public boolean isPublic() {
    return getImportLabelByType(ProtoTokenTypes.PUBLIC) != null;
  }

  @Override
  public boolean isWeak() {
    return getImportLabelByType(ProtoTokenTypes.WEAK) != null;
  }

  @Override
  public boolean isOption() {
    return getImportLabelByType(ProtoTokenTypes.OPTION) != null;
  }
}
