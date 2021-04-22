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

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.protobuf.lang.psi.util.PbPsiUtil.*;

/** Conditions to restrict resolve results. */
public class ResolveFilters {

  // Static instances for filters that require no parameterization.

  private static final Condition<PbSymbol> anySymbol = (element) -> true;

  private static final Condition<PbSymbol> packageOrMessage =
      (element) -> isPackageElement(element) || isMessageElement(element);

  private static final Condition<PbSymbol> packageOrMessageWithExtension =
      (element) ->
          isPackageElement(element)
              || (isMessageElement(element) && messageHasExtension((PbMessageType) element));

  private static final Condition<PbSymbol> packageOrType =
      (element) -> isPackageElement(element) || isTypeElement(element);

  private static final Condition<PbSymbol> symbolOwner = PbSymbolOwner.class::isInstance;

  private static final Condition<PbSymbol> unsuggestableFilter = PbPsiUtil::isGeneratedMapEntry;

  private static boolean messageHasExtension(PbMessageType message) {
    return message
        .getStatements()
        .stream()
        .anyMatch(
            statement ->
                PbPsiUtil.isExtendElement(statement)
                    || (statement instanceof PbMessageType
                        && messageHasExtension((PbMessageType) statement)));
  }

  /** Matches any PbSymbol. */
  @NotNull
  public static Condition<PbSymbol> anySymbol() {
    return anySymbol;
  }

  /** Matches any PbPackageName or PbMessageType. */
  @NotNull
  public static Condition<PbSymbol> packageOrMessage() {
    return packageOrMessage;
  }

  /** Matches any PbPackageName or a PbMessageType the contains an extension. */
  @NotNull
  public static Condition<PbSymbol> packageOrMessageWithExtension() {
    return packageOrMessageWithExtension;
  }

  /** Matches any PbPackageName or PbNamedTypeElement. */
  @NotNull
  public static Condition<PbSymbol> packageOrType() {
    return packageOrType;
  }

  /** Matches any PbSymbolOwner. */
  @NotNull
  public static Condition<PbSymbol> symbolOwner() {
    return symbolOwner;
  }

  /** Matches fields that extend the given type. */
  @NotNull
  public static Condition<PbSymbol> extendedFromType(PbNamedTypeElement typeElement) {
    return (element) ->
        element instanceof PbField && PbPsiUtil.fieldIsExtension((PbField) element, typeElement);
  }

  /** Matches fields of the given type, or fields that extend it. */
  @NotNull
  public static Condition<PbSymbol> extendedFromTypeOrMember(PbNamedTypeElement typeElement) {
    return (element) ->
        element instanceof PbField
            && PbPsiUtil.fieldIsExtensionOrMember((PbField) element, typeElement);
  }

  /** Returns the base filter with additional filtering of unsuggestable items. */
  @Nullable
  public static Condition<PbSymbol> withUnsuggestableFilter(@Nullable Condition<PbSymbol> base) {
    if (base == null) {
      return null;
    }
    return Conditions.and(base, Conditions.not(unsuggestableFilter()));
  }

  /** Returns a predicate that matches elements that should NOT be suggested. */
  private static Condition<PbSymbol> unsuggestableFilter() {
    return unsuggestableFilter;
  }

  private ResolveFilters() {}
}
