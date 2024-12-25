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
import com.intellij.protobuf.lang.psi.PbStringValue;
import com.intellij.protobuf.lang.psi.PbSyntaxStatement;
import com.intellij.protobuf.lang.psi.SyntaxLevel;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbSyntaxStatementMixin extends PbStatementBase implements PbSyntaxStatement {

  PbSyntaxStatementMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable SyntaxLevel getSyntaxLevel() {
    String syntaxType = getSyntaxTypeExpression().getText();
    PbStringValue syntaxVersionPsi = getSyntaxVersionExpression();
    String syntaxVersion = syntaxVersionPsi == null ? "" : syntaxVersionPsi.getAsString();
    return SyntaxLevel.parse(syntaxType, syntaxVersion);
  }

  @Override
  public @NotNull PsiElement getSyntaxTypeExpression() {
    return getFirstChild();
  }

  @Override
  public @Nullable PbStringValue getSyntaxVersionExpression() {
    return getStringValue();
  }
}
