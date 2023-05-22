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
package com.intellij.protobuf.lang.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A {@link PsiReference} implementation for {@link PbOptionName}. */
public class PbOptionNameReference extends PsiReferenceBase<PbOptionName> {

  public PbOptionNameReference(@NotNull PbOptionName element) {
    super(element);
  }

  @Nullable
  @Override
  protected TextRange calculateDefaultRangeInElement() {
    PsiElement symbol = myElement.getSymbol();
    if (symbol == null) {
      return null;
    }
    TextRange outerRange = myElement.getTextRange();
    TextRange identifierRange = symbol.getTextRange();
    return TextRange.from(
        identifierRange.getStartOffset() - outerRange.getStartOffset(),
        identifierRange.getLength());
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    ResolveCache cache = ResolveCache.getInstance(myElement.getProject());
    return cache.resolveWithCaching(
        this, Resolver.INSTANCE, /* needToPreventRecursion= */ false, /* incompleteCode= */ false);
  }

  private PbField resolveNoCache() {
    PsiElement symbol = myElement.getSymbol();
    if (symbol == null) {
      return null;
    }
    String name = symbol.getText();
    return resolveNamedFieldInType(name, myElement.getQualifierType());
  }

  @Nullable
  private static PbField resolveNamedFieldInType(String name, PbMessageType type) {
    if (type == null) {
      return null;
    }

    Collection<PbSymbol> pbSymbols = type.getSymbolMap().get(name);
    if (pbSymbols == null || pbSymbols.isEmpty()) return null;
    return pbSymbols.stream()
        .filter(symbol -> symbol instanceof PbField)
        .map(symbol -> (PbField) symbol)
        .filter(field -> !field.isExtension())
        .findFirst()
        .orElse(null);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    PbMessageType qualifierType = myElement.getQualifierType();
    if (qualifierType == null) {
      return LookupElement.EMPTY_ARRAY;
    }
    OptionOccurrenceTracker.Occurrence occurrence = getOccurrence();
    Collection<PbField> fields = qualifierType.getSymbols(PbField.class);
    List<LookupElement> variants = new ArrayList<>(fields.size());
    for (PbField field : fields) {
      if (field.isExtension()) {
        continue;
      }
      if (field.getName() == null) {
        continue;
      }
      variants.add(PbSymbolLookupElement.withUnmergeableFieldHighlight(field, occurrence));
    }
    return variants.toArray();
  }

  private OptionOccurrenceTracker.Occurrence getOccurrence() {
    PbOptionOwner owner = PbPsiImplUtil.getOptionOwner(getElement());
    if (owner == null) {
      return null;
    }
    OptionOccurrenceTracker tracker = OptionOccurrenceTracker.forOptionOwner(owner);
    PbOptionName qualifier = getElement().getQualifier();
    if (qualifier != null) {
      return tracker.getOccurrence(qualifier);
    } else {
      return tracker.getRootOccurrence();
    }
  }

  private static class Resolver implements ResolveCache.Resolver {
    private static final Resolver INSTANCE = new Resolver();

    @Nullable
    @Override
    public PsiElement resolve(@NotNull PsiReference ref, boolean incompleteCode) {
      return ((PbOptionNameReference) ref).resolveNoCache();
    }
  }
}
