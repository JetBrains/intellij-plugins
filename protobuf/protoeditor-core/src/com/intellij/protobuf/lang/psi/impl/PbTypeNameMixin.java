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
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.resolve.PbSymbolResolver;
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference;
import com.intellij.protobuf.lang.resolve.ResolveFilters;
import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbTypeNameMixin extends PbQualifiedReferenceBase implements PbTypeName {

  PbTypeNameMixin(ASTNode node) {
    super(node);
  }

  @Override
  public BuiltInType getBuiltInType() {
    ProtoSymbolPath path = getSymbolPath();
    if (isFullyQualified() || path.getQualifier() != null) {
      // Qualified names can't be built-in types.
      return null;
    }
    return BuiltInType.getType(path.getSymbol().getText());
  }

  @NotNull
  @Override
  public ProtoSymbolPathDelegate getDefaultPathDelegate() {
    return new ProtoSymbolPathDelegate() {
      @Override
      public PsiReference getReference(ProtoSymbolPath path) {
        if (getBuiltInType() != null) {
          // Built-in types shouldn't generate references.
          return null;
        }

        QualifiedName scope;
        if (!isFullyQualified()) {
          PbSymbolOwner owner = PbPsiUtil.getSymbolOwner(path);
          scope = owner != null ? owner.getChildScope() : null;
        } else {
          scope = null;
        }
        return new ProtoSymbolPathReference(
            path,
            PbSymbolResolver.forFile(getPbFile()),
            scope,
            ResolveFilters.packageOrType(),
            ResolveFilters.withUnsuggestableFilter(getPackageOrTypeFilter()));
      }
    };
  }

  @NotNull
  @Override
  public String getShortName() {
    return getSymbolPath().getSymbol().getText();
  }

  @Nullable
  @Override
  public PsiReference getEffectiveReference() {
    return getSymbolPath().getReference();
  }

  @Nullable
  private Condition<PbSymbol> getPackageOrTypeFilter() {
    // Types for Fields can have Messages or Enums.
    // For MapFields, keys can only be builtin types. The value type can be message or enum.
    // Types for "extend T" can only be MessageTypes
    // Service method param and return types can only be MessageTypes
    PbField fieldParent = PsiTreeUtil.getParentOfType(this, PbField.class);
    if (fieldParent != null) {
      if (fieldParent instanceof PbMapField) {
        if (this.equals(((PbMapField) fieldParent).getKeyType())) {
          return null;
        }
      }
      return ResolveFilters.packageOrType();
    } else if (this instanceof PbMessageTypeName) {
      // MessageTypeName is an element used for service method params and extend definitions.
      return ResolveFilters.packageOrMessage();
    }
    return null;
  }
}
