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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.resolve.PbOptionNameReference;
import com.intellij.protobuf.lang.resolve.PbResolveResult;
import com.intellij.protobuf.lang.resolve.PbSymbolResolver;
import com.intellij.protobuf.lang.resolve.ResolveFilters;
import com.intellij.protobuf.lang.util.BuiltInType;
import com.intellij.protobuf.lang.util.ValueTester;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

abstract class PbOptionNameMixin extends PbElementBase implements PbOptionName {

  private static final Logger logger = Logger.getInstance(PbOptionNameMixin.class);

  PbOptionNameMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PbOptionName getQualifier() {
    return PsiTreeUtil.getChildOfType(this, PbOptionName.class);
  }

  @Override
  public @Nullable PbExtensionName getExtensionName() {
    return PsiTreeUtil.getChildOfType(this, PbExtensionName.class);
  }

  @Override
  public @Nullable PsiElement getSymbol() {
    return findChildByType(ProtoTokenTypes.IDENTIFIER_LITERAL);
  }

  @Override
  public PsiReference getReference() {
    // Return a reference if this is not a special built-in option. If this option is an extension,
    // return null and let the ExtensionName element handle the reference.
    if (getExtensionName() != null || isSpecial()) {
      return null;
    }
    return new PbOptionNameReference(this);
  }

  @Override
  public @Nullable PsiReference getEffectiveReference() {
    // Return a reference if this is not a special built-in option. If this option is an extension,
    // return the reference from getExtensionName().getEffectiveReference().
    PbExtensionName extensionName = getExtensionName();
    if (extensionName != null) {
      return extensionName.getEffectiveReference();
    }
    return getReference();
  }

  @Override
  public @Nullable PbMessageType getQualifierType() {
    return CachedValuesManager.getCachedValue(
        this,
        () -> Result.create(getQualifierTypeNoCache(), PbCompositeModificationTracker.byElement(this)));
  }

  private @Nullable PbMessageType getQualifierTypeNoCache() {
    PbOptionName qualifier = getQualifier();
    if (qualifier != null) {
      return getFieldType(getQualifierField(qualifier));
    } else {
      return getDescriptorType();
    }
  }

  @Override
  public boolean isSpecial() {
    return getSpecialType() != null;
  }

  @Override
  public SpecialOptionType getSpecialType() {
    PsiElement symbol = getSymbol();
    if (symbol != null) {
      PbOptionOwner owner = getOwner();
      for (SpecialOptionType type : SpecialOptionType.values()) {
        if (type.isInstance(this, owner)) {
          return type;
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable ValueTester getBuiltInValueTester() {
    BuiltInType type = getBuiltInType();
    if (type == null) {
      return null;
    }
    SpecialOptionType specialType = getSpecialType();
    if (specialType == SpecialOptionType.FIELD_DEFAULT) {
      return type.getValueTester(ValueTester.ValueTesterType.DEFAULT);
    } else {
      return type.getValueTester(ValueTester.ValueTesterType.OPTION);
    }
  }

  @Override
  public @Nullable BuiltInType getBuiltInType() {
    SpecialOptionType specialType = getSpecialType();
    if (specialType == SpecialOptionType.FIELD_DEFAULT) {
      PbOptionOwner owner = getOwner();
      if (!(owner instanceof PbField)) {
        return null;
      }
      PbTypeName typeName = ((PbField) owner).getTypeName();
      if (typeName == null) {
        return null;
      }
      return typeName.getBuiltInType();
    } else if (specialType == SpecialOptionType.FIELD_JSON_NAME) {
      return BuiltInType.STRING;
    }

    // Other non-special options
    PsiReference ref = getEffectiveReference();
    if (ref == null) {
      return null;
    }
    PsiElement optionField = ref.resolve();
    if (!(optionField instanceof PbField)) {
      return null;
    }
    PbTypeName typeName = ((PbField) optionField).getTypeName();
    if (typeName == null) {
      return null;
    }
    return typeName.getBuiltInType();
  }

  @Override
  public @Nullable PbNamedTypeElement getNamedType() {
    PbTypeName typeName = null;

    SpecialOptionType specialType = getSpecialType();
    if (specialType == SpecialOptionType.FIELD_DEFAULT) {
      PbOptionOwner owner = getOwner();
      if (!(owner instanceof PbField)) {
        return null;
      }
      typeName = ((PbField) owner).getTypeName();
    } else if (specialType == null) {
      PsiReference ref = getEffectiveReference();
      if (ref == null) {
        return null;
      }
      PsiElement optionField = ref.resolve();
      if (!(optionField instanceof PbField)) {
        return null;
      }
      typeName = ((PbField) optionField).getTypeName();
    }

    if (typeName != null) {
      PsiReference ref = typeName.getEffectiveReference();
      if (ref == null) {
        return null;
      }
      PsiElement resolved = ref.resolve();
      if (resolved instanceof PbNamedTypeElement) {
        return (PbNamedTypeElement) resolved;
      }
    }

    return null;
  }

  private static @Nullable PbMessageType getFieldType(PbField field) {
    if (field == null) {
      return null;
    }
    PbTypeName type = field.getTypeName();
    if (type == null) {
      return null;
    }
    return PbPsiUtil.resolveRefToType(type.getEffectiveReference(), PbMessageType.class);
  }

  private @Nullable PbMessageType getDescriptorType() {
    PbOptionOwner owner = getOwner();
    if (owner == null) {
      // This shouldn't be possible with the proto BNF.
      logger.error("PbOptionName has no PbOptionOwner: " + this);
      return null;
    }
    Descriptor descriptor = Descriptor.locate(getPbFile());
    if (descriptor == null) {
      return null;
    }
    return PbSymbolResolver.forFile(descriptor.getFile())
        .resolveName(
            owner.getDescriptorOptionsTypeName(descriptor), ResolveFilters.packageOrMessage())
        .stream()
        .map(PbResolveResult::getElement)
        .filter(element -> element instanceof PbMessageType)
        .map(element -> (PbMessageType) element)
        .findFirst()
        .orElse(null);
  }

  private static @Nullable PbField getQualifierField(PbOptionName qualifier) {
    PsiReference ref = qualifier.getEffectiveReference();
    if (ref != null) {
      return PbPsiUtil.resolveRefToType(ref, PbField.class);
    }
    return null;
  }

  private @Nullable PbOptionOwner getOwner() {
    return PbPsiImplUtil.getOptionOwner(this);
  }
}
