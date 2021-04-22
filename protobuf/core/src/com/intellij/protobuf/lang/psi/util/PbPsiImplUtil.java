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
package com.intellij.protobuf.lang.psi.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class contains static utility methods that are used by various PSI implementations.
 * Grammar-Kit looks at this class for methods named in the grammar source.
 */
public final class PbPsiImplUtil {

  @Nullable
  public static PbStatementOwner getStatementOwner(PbStatement element) {
    return PsiTreeUtil.getParentOfType(element, PbStatementOwner.class);
  }

  @Nullable
  public static PbSymbolOwner getSymbolOwner(PsiElement element) {
    return PbPsiUtil.getSymbolOwner(element);
  }

  @Nullable
  public static PbOptionOwner getOptionOwner(PsiElement element) {
    // For finding PbOptionOwner parents, we use getContext() instead of getParent(). Group
    // definitions generate an associated field, and the options specified along with the group are
    // tied to the generated field. See PbGroupDefinitionMixin.
    // Also, this method can be called on PbText* elements within an injected PbTextFile to get the
    // outer PbOptionOwner.
    return PsiTreeUtil.getContextOfType(element, PbOptionOwner.class, true);
  }

  @NotNull
  public static List<PbStatement> getStatements(PbElement parent) {
    return PsiTreeUtil.getChildrenOfTypeAsList(parent, PbStatement.class);
  }

  @Nullable
  public static QualifiedName getQualifiedName(final PbSymbol element) {
    return CachedValuesManager.getCachedValue(
        element,
        () ->
            Result.create(
                calculateQualifiedName(element), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Nullable
  private static QualifiedName calculateQualifiedName(PbSymbol element) {
    String name = element.getName();
    if (name == null) {
      return null;
    }
    PbSymbolOwner parent = element.getSymbolOwner();
    if (parent == null) {
      return null;
    }
    QualifiedName scope = parent.getChildScope();
    if (scope == null) {
      return null;
    }
    return scope.append(name);
  }

  /**
   * Returns a TextRange for the element without starting and ending quotes. If the quotes are
   * incomplete (missing end quote), the start and end offsets will be the same.
   */
  @NotNull
  public static TextRange getTextRangeNoQuotes(PsiElement element) {
    TextRange range = element.getTextRange();
    String text = element.getText();
    char endQuote;
    if (text.startsWith("\"") || text.startsWith("'")) {
      endQuote = text.charAt(0);
    } else {
      return range;
    }
    int endOffset = text.charAt(text.length() - 1) == endQuote ? 1 : 0;
    int start = range.getStartOffset() + 1;
    int end = range.getEndOffset() - endOffset;
    if (end < start) {
      end = start;
    }
    return TextRange.create(start, end);
  }

  public static ImmutableMultimap<String, PbSymbol> getCachedSymbolMap(PbStatementOwner owner) {
    return CachedValuesManager.getCachedValue(
        owner,
        () ->
            Result.create(
                computeSymbolMap(owner, PbSymbol.class),
                PsiModificationTracker.MODIFICATION_COUNT));
  }

  public static ImmutableMultimap<String, PbEnumValue> getCachedEnumValueMap(
      PbStatementOwner owner) {
    return CachedValuesManager.getCachedValue(
        owner,
        () ->
            Result.create(
                computeSymbolMap(owner, PbEnumValue.class),
                PsiModificationTracker.MODIFICATION_COUNT));
  }

  private static <T extends PbSymbol> ImmutableMultimap<String, T> computeSymbolMap(
      PbStatementOwner owner, Class<T> typeClass) {
    List<PbStatement> statements = owner.getStatements();
    ImmutableSetMultimap.Builder<String, T> builder = ImmutableSetMultimap.builder();
    for (PbStatement statement : statements) {
      if (typeClass.isInstance(statement)) {
        T symbol = typeClass.cast(statement);
        String name = symbol.getName();
        if (name != null) {
          builder.put(name, symbol);
        }
      }
      if (statement instanceof PbSymbolContributor) {
        PbSymbolContributor contributor = (PbSymbolContributor) statement;
        for (PbSymbol sibling : contributor.getAdditionalSiblings()) {
          if (typeClass.isInstance(sibling)) {
            String name = sibling.getName();
            if (name != null) {
              builder.put(name, typeClass.cast(sibling));
            }
          }
        }
      }
      if (statement instanceof PbStatementOwner && !(statement instanceof PbSymbolOwner)) {
        // This is statement owner that does not define a symbol scope, such as an extend or oneof
        // definition. We grab all of the symbols under it and add them to the current scope.
        builder.putAll(computeSymbolMap((PbStatementOwner) statement, typeClass));
      }
    }
    return builder.build();
  }

  private PbPsiImplUtil() {}
}
