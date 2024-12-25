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
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.resolve.*;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PbTextExtensionNameMixin extends PbTextElementBase implements PbTextExtensionName {

  PbTextExtensionNameMixin(ASTNode node) {
    super(node);
  }

  private QualifiedName getRelativeScope(ProtoSymbolPath path) {
    // Any type URLs are always absolute. Extension names are relative when used in .proto files,
    // but absolute in standalone files.
    if (!(getContainingFile() instanceof PbFile) || isAnyTypeUrl()) {
      return null;
    }

    PbTextMessage message = PsiTreeUtil.getParentOfType(path, PbTextMessage.class);
    if (message == null) {
      return null;
    }
    PbMessageType declaredMessage = message.getDeclaredMessage();
    if (declaredMessage == null) {
      return null;
    }

    // The relative resolution actually starts from the message's parent scope. This only makes
    // a difference in the extreme edge case that the extension field appears somewhere under the
    // scope of the message it's extending.
    PbSymbolOwner owner = declaredMessage.getSymbolOwner();
    return owner != null ? owner.getChildScope() : null;
  }

  @Override
  public @NotNull ProtoSymbolPathDelegate getDefaultPathDelegate() {
    return new ProtoSymbolPathDelegate() {
      @Override
      public @Nullable PsiReference getReference(ProtoSymbolPath path) {
        QualifiedName scope = getRelativeScope(path);
        PbTextRootMessage rootMessage = getRootMessage();
        if (rootMessage == null) {
          return null;
        }
        SchemaInfo schemaInfo = rootMessage.getSchemaInfo();
        if (schemaInfo == null) {
          return null;
        }
        PbSymbolResolver resolver = schemaInfo.getExtensionResolver();

        PbTextMessage parentMessage =
            PsiTreeUtil.getParentOfType(PbTextExtensionNameMixin.this, PbTextMessage.class);
        if (parentMessage == null) {
          return null;
        }
        PbMessageType declaredMessage = parentMessage.getDeclaredMessage();
        if (declaredMessage == null) {
          return null;
        }

        final Condition<PbSymbol> filter;
        if (getDomain() == null) {
          // Extension name - the name should be a field.
          filter = getExtensionFilter(declaredMessage);
        } else {
          // Any type url - the name should be a package or message type.
          filter = ResolveFilters.packageOrMessage();
        }

        return new ProtoSymbolPathReference(
            path,
            resolver,
            scope,
            ResolveFilters.anySymbol(),
            ResolveFilters.withUnsuggestableFilter(filter),
            (symbol) -> PbSymbolLookupElement.withUnusableFieldHighlight(symbol, getOccurrence()));
      }
    };
  }

  private static Condition<PbSymbol> getExtensionFilter(PbMessageType declaredMessage) {
    // The symbol path of a PbTextExtensionName can refer to:
    // - a package or message that contains an extend definition (serves as a scope).
    // - or, a field that is part of an extend definition.
    Condition<PbSymbol> base = ResolveFilters.packageOrMessageWithExtension();
    return Conditions.or(base, ResolveFilters.extendedFromType(declaredMessage));
  }

  @Override
  public @Nullable PsiReference getEffectiveReference() {
    PbTextSymbolPath symbolPath = getSymbolPath();
    if (symbolPath == null) {
      return null;
    }
    PsiReference symbolPathRef = symbolPath.getReference();
    if (symbolPathRef == null) {
      return null;
    }
    return new PbTextMessageSetSupportingExtensionNameReference(symbolPathRef);
  }

  private OptionOccurrenceTracker.Occurrence getOccurrence() {

    PbTextMessage parent = PsiTreeUtil.getParentOfType(this, PbTextMessage.class);
    if (parent == null) {
      return null;
    }
    OptionOccurrenceTracker tracker = OptionOccurrenceTracker.forMessage(parent);
    if (tracker == null) {
      return null;
    }
    return tracker.getOccurrence(parent);
  }
}
