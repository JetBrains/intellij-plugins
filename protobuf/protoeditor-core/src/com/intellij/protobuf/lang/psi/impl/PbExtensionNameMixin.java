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
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.resolve.PbSymbolLookupElement;
import com.intellij.protobuf.lang.resolve.PbSymbolResolver;
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference;
import com.intellij.protobuf.lang.resolve.ResolveFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbExtensionNameMixin extends PbQualifiedReferenceBase implements PbExtensionName {

  PbExtensionNameMixin(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public ProtoSymbolPathDelegate getDefaultPathDelegate() {
    return new ProtoSymbolPathDelegate() {
      @Override
      public PsiReference getReference(ProtoSymbolPath path) {
        QualifiedName scope;
        if (!isFullyQualified()) {
          PbOptionOwner optionOwner = PbPsiImplUtil.getOptionOwner(path);
          scope = optionOwner != null ? optionOwner.getExtensionOptionScope() : null;
        } else {
          scope = null;
        }
        return new ProtoSymbolPathReference(
            path,
            PbSymbolResolver.forFile(getPbFile()),
            scope,
            ResolveFilters.anySymbol(),
            ResolveFilters.withUnsuggestableFilter(getExtensionFilter()),
            (symbol) ->
                PbSymbolLookupElement.withUnmergeableFieldHighlight(symbol, getOccurrence()));
      }
    };
  }

  @Nullable
  @Override
  public PsiReference getEffectiveReference() {
    return getSymbolPath().getReference();
  }

  @Nullable
  private Condition<PbSymbol> getExtensionFilter() {
    PbOptionName optionName = PsiTreeUtil.getParentOfType(this, PbOptionName.class);
    if (optionName == null) {
      return null;
    }
    PbNamedTypeElement qualifierType = optionName.getQualifierType();
    if (qualifierType == null) {
      return null;
    }
    QualifiedName qualifierTypeName = qualifierType.getQualifiedName();
    if (qualifierTypeName == null) {
      return null;
    }
    // The symbol path of a PbExtensionName can refer to:
    // - a package or message that contains an extend definition (serves as a scope).
    // - or, a field that is an extension of qualifierType.
    // - or, a member of qualifierType.
    // - or, qualifierType itself.
    Condition<PbSymbol> condition =
        Conditions.or(
            ResolveFilters.packageOrMessageWithExtension(),
            ResolveFilters.extendedFromTypeOrMember(qualifierType));
    condition =
        Conditions.or(condition, symbol -> qualifierTypeName.equals(symbol.getQualifiedName()));

    return condition;
  }

  private OptionOccurrenceTracker.Occurrence getOccurrence() {
    PbOptionName name = PsiTreeUtil.getParentOfType(this, PbOptionName.class);
    if (name == null) {
      return null;
    }
    PbOptionOwner owner = PbPsiImplUtil.getOptionOwner(name);
    if (owner == null) {
      return null;
    }
    OptionOccurrenceTracker tracker = OptionOccurrenceTracker.forOptionOwner(owner);
    PbOptionName qualifier = name.getQualifier();
    if (qualifier != null) {
      return tracker.getOccurrence(qualifier);
    } else {
      return tracker.getRootOccurrence();
    }
  }
}
