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
import com.intellij.protobuf.lang.psi.ProtoSymbolPath;
import com.intellij.protobuf.lang.psi.ProtoSymbolPathContainer;
import com.intellij.protobuf.lang.psi.ProtoSymbolPathDelegate;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PbTextSymbolPathMixin extends PbTextElementBase implements ProtoSymbolPath {

  PbTextSymbolPathMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull PsiElement getSymbol() {
    return findNotNullChildByType(ProtoTokenTypes.IDENTIFIER_LITERAL);
  }

  @Override
  public int getTextOffset() {
    PsiElement name = getNameIdentifier();
    return name != null ? name.getTextOffset() : super.getTextOffset();
  }

  @Override
  public @Nullable PsiReference getReference() {
    ProtoSymbolPathDelegate delegate = getPathDelegate();
    if (delegate != null) {
      return delegate.getReference(this);
    }
    return null;
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    ProtoSymbolPathDelegate delegate = getPathDelegate();
    if (delegate != null) {
      return delegate.getNameIdentifier(this);
    }
    return null;
  }

  @Override
  public @Nullable String getName() {
    ProtoSymbolPathDelegate delegate = getPathDelegate();
    if (delegate != null) {
      return delegate.getName(this);
    }
    return null;
  }

  @Override
  public @Nullable PsiElement setName(@NonNls @NotNull String name) {
    ProtoSymbolPathDelegate delegate = getPathDelegate();
    if (delegate != null) {
      return delegate.setName(this, name);
    }
    throw new IncorrectOperationException();
  }

  private @Nullable ProtoSymbolPathDelegate getPathDelegate() {
    ProtoSymbolPathContainer container = getPathContainer();
    if (container != null) {
      return container.getPathDelegate();
    }
    return null;
  }
}
