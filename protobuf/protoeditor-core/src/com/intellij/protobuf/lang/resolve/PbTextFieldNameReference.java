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
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A {@link PsiReference} implementation for {@link PbTextFieldName}. */
public class PbTextFieldNameReference extends PsiReferenceBase<PbTextFieldName> {

  public PbTextFieldNameReference(@NotNull PbTextFieldName element) {
    super(element);
  }

  @Nullable
  @Override
  protected TextRange calculateDefaultRangeInElement() {
    return TextRange.create(0, myElement.getTextLength());
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    ResolveCache cache = ResolveCache.getInstance(myElement.getProject());
    return cache.resolveWithCaching(
        this, Resolver.INSTANCE, /* needToPreventRecursion= */ false, /* incompleteCode= */ false);
  }

  private PbField resolveNoCache() {
    PsiElement identifier = myElement.getNameIdentifier();
    if (identifier == null) {
      return null;
    }
    String name = identifier.getText();
    return resolveNamedFieldInType(name, getContainingMessage(myElement));
  }

  private static PbMessageType getContainingMessage(PbTextFieldName name) {
    PbTextMessage parentMessage = PsiTreeUtil.getParentOfType(name, PbTextMessage.class);
    if (parentMessage == null) {
      return null;
    }
    return parentMessage.getDeclaredMessage();
  }

  @Nullable
  private static PbField resolveNamedFieldInType(String name, PbMessageType type) {
    if (type == null) {
      return null;
    }

    PbSymbol symbol =
        type.getSymbolMap().get(name).stream()
            .filter(s -> s instanceof PbField || s instanceof PbGroupDefinition)
            .findFirst()
            .orElse(null);

    if (symbol instanceof PbGroupDefinition) {
      PbField generatedField = ((PbGroupDefinition) symbol).getGeneratedField();
      if (generatedField == null || generatedField.isExtension()) {
        return null;
      }
      return generatedField;
    }

    if (symbol instanceof PbField) {
      PbField field = (PbField) symbol;
      if (field.isExtension()) {
        return null;
      }

      PbTypeName fieldType = field.getTypeName();
      if (fieldType == null) {
        return null;
      }

      // Make sure that group fields use the capitalized type name rather than the lowercase field
      // name.
      PbGroupDefinition group =
          PbPsiUtil.resolveRefToType(fieldType.getEffectiveReference(), PbGroupDefinition.class);
      if (group != null && !name.equals(group.getName())) {
        return null;
      }

      return field;
    }

    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    PbMessageType qualifierType = getContainingMessage(myElement);
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
      PbTypeName fieldType = field.getTypeName();
      if (fieldType == null) {
        continue;
      }
      PbGroupDefinition group =
          PbPsiUtil.resolveRefToType(fieldType.getEffectiveReference(), PbGroupDefinition.class);
      if (group != null) {
        variants.add(PbSymbolLookupElement.forGroupDefinitionAsField(group, occurrence));
      } else {
        variants.add(PbSymbolLookupElement.withUnusableFieldHighlight(field, occurrence));
      }
    }
    return variants.toArray();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) {
    PsiElement identifier = getElement().getNameIdentifier();
    if (identifier != null) {
      ASTNode node = identifier.getNode();
      if (node instanceof LeafElement) {
        ((LeafElement) node).replaceWithText(newElementName);
        return identifier;
      }
    }
    return super.handleElementRename(newElementName);
  }

  private OptionOccurrenceTracker.Occurrence getOccurrence() {
    PbTextMessage parent = PsiTreeUtil.getParentOfType(getElement(), PbTextMessage.class);
    if (parent == null) {
      return null;
    }
    OptionOccurrenceTracker tracker = OptionOccurrenceTracker.forMessage(parent);
    if (tracker == null) {
      return null;
    }
    return tracker.getOccurrence(parent);
  }

  private static class Resolver implements ResolveCache.Resolver {
    private static final Resolver INSTANCE = new Resolver();

    @Nullable
    @Override
    public PsiElement resolve(@NotNull PsiReference ref, boolean incompleteCode) {
      return ((PbTextFieldNameReference) ref).resolveNoCache();
    }
  }
}
