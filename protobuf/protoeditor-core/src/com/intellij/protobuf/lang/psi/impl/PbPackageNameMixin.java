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
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.psi.PbPackageName;
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.protobuf.lang.psi.PbSymbolOwner;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

abstract class PbPackageNameMixin extends PbElementBase implements PbPackageName {

  PbPackageNameMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PbPackageName getQualifier() {
    return PsiTreeUtil.getChildOfType(this, PbPackageName.class);
  }

  @Nullable
  @Override
  public QualifiedName getQualifiedName() {
    return PbPsiImplUtil.getQualifiedName(this);
  }

  @Nullable
  @Override
  public PbSymbolOwner getSymbolOwner() {
    PbPackageName qualifier = getQualifier();
    return qualifier != null ? getQualifier() : getPbFile();
  }

  @Override
  public int getTextOffset() {
    return getNameIdentifier().getTextOffset();
  }

  @NotNull
  @Override
  public PsiElement getNameIdentifier() {
    return findNotNullChildByType(ProtoTokenTypes.IDENTIFIER_LITERAL);
  }

  @NotNull
  @Override
  public String getName() {
    return getNameIdentifier().getText();
  }

  @Nullable
  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiElement identifier = getNameIdentifier();
    ASTNode node = identifier.getNode();
    if (node instanceof LeafElement) {
      ((LeafElement) node).replaceWithText(name);
      return this;
    }
    throw new IncorrectOperationException();
  }

  @Nullable
  @Override
  public QualifiedName getChildScope() {
    return getQualifiedName();
  }

  @NotNull
  @Override
  public Map<String, Collection<PbSymbol>> getSymbolMap() {
    return getPbFile().getPackageSymbolMap(getQualifiedName());
  }

  @Override
  public Icon getIcon(int flags) {
    return PbIcons.PACKAGE;
  }
}
