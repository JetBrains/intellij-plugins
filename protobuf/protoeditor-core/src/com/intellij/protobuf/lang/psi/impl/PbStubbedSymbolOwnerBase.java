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
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.protobuf.lang.psi.PbSymbolOwner;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.stub.PbNamedElementStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

abstract class PbStubbedSymbolOwnerBase<T extends PbNamedElementStub<?>>
    extends PbStubbedNamedDefinitionBase<T> implements PbSymbolOwner {

  PbStubbedSymbolOwnerBase(ASTNode node) {
    super(node);
  }

  PbStubbedSymbolOwnerBase(T stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Override
  public @Nullable QualifiedName getChildScope() {
    return getQualifiedName();
  }

  @Override
  public @NotNull Map<String, Collection<PbSymbol>> getSymbolMap() {
    return PbPsiImplUtil.getCachedSymbolMap(this);
  }

  @Override
  public boolean processDeclarations(
      @NotNull PsiScopeProcessor processor,
      @NotNull ResolveState state,
      PsiElement lastParent,
      @NotNull PsiElement place) {
    for (PbSymbol symbol : getSymbols()) {
      if (!processor.execute(symbol, state)) {
        return false;
      }
    }
    return true;
  }
}
