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
package com.intellij.protobuf.lang.annotation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.intentions.PbAddImportStatementIntention;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Annotations shared between proto and text format annotators. */
public final class SharedAnnotations {

  /** Psi reference states. */
  public enum ReferenceState {
    VALID,
    UNRESOLVED,
    AMBIGUOUS,
    NULL
  }

  static ReferenceState getReferenceState(PsiReference reference) {
    if (reference == null) {
      return ReferenceState.NULL;
    } else if (reference instanceof PsiPolyVariantReference) {
      ResolveResult[] results = ((PsiPolyVariantReference) reference).multiResolve(false);
      if (results.length == 0) {
        return ReferenceState.UNRESOLVED;
      } else if (results.length > 1) {
        return ReferenceState.AMBIGUOUS;
      } else {
        return ReferenceState.VALID;
      }
    } else {
      if (reference.resolve() == null) {
        return ReferenceState.UNRESOLVED;
      } else {
        return ReferenceState.VALID;
      }
    }
  }

  /** Annotate issues with enum {@link ProtoIdentifierValue values}. */
  static void annotateEnumOptionValue(
      @NotNull ProtoIdentifierValue identifierValue, @NotNull AnnotationHolder holder) {
    annotateEnumOptionValue((PsiElement) identifierValue, holder);
  }

  /** Annotate issues with enum {@link ProtoNumberValue values}. */
  static void annotateEnumOptionValue(
      @NotNull ProtoNumberValue numberValue, @NotNull AnnotationHolder holder) {
    annotateEnumOptionValue((PsiElement) numberValue, holder);
  }

  private static void annotateEnumOptionValue(PsiElement enumElement, AnnotationHolder holder) {
    switch (getReferenceState(enumElement.getReference())) {
      case VALID, NULL -> {
        // A null reference indicates that the value type isn't an enum.
      }
      default -> holder.newAnnotation(
          HighlightSeverity.ERROR, PbLangBundle.message("cannot.resolve.enum.value", enumElement.getText()))
        .range(enumElement.getTextRange())
        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
        .create();
    }
  }

  /** Annotate issues with {@link ProtoStringPart} elements. */
  static void annotateStringPart(
      ProtoStringPart part, AnnotationHolder holder, TextAttributesKey invalidEscapeAttributes) {
    if (part.isUnterminated()) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("unterminated.string"))
          .range(part.getTextRange())
          .afterEndOfLine()
          .create();
    }
    for (TextRange range : part.getInvalidEscapeRanges()) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("illegal.escape.sequence"))
          .range(range.shiftRight(part.getTextOffset()))
          .textAttributes(invalidEscapeAttributes)
          .create();
    }
  }

  /**
   * Annotate {@link ProtoSymbolPath} references.
   *
   * <p>There are two types of reference errors for symbol paths: unresolved and ambiguous.
   *
   * <p>For unresolved errors, we want the error to apply to the first unresolved symbol in the
   * path. E.g., in "com.invalid.foo", the error should be under "invalid" rather than "foo", even
   * though "foo" is also unresolved due to "invalid" being unresolved.
   *
   * <p>Ambiguous errors only apply to the last element in the path. Even though an intermediate
   * symbol can be ambiguous, this only occurs if the last symbol is itself ambiguous or undefined.
   * In both cases, we prefer placing the error on the last symbol.
   *
   * @param path The symbol path to annotate.
   * @param holder The annotation holder for this session.
   * @return <code>true</code> if an annotation was added to this path or its qualifier.
   */
  static boolean annotateSymbolPath(
      @NotNull ProtoSymbolPath path, @NotNull AnnotationHolder holder) {
    ProtoSymbolPath qualifier = path.getQualifier();
    if (qualifier != null && annotateSymbolPath(qualifier, holder)) {
      return true;
    }

    ProtoSymbolPathContainer container = path.getPathContainer();
    if (container instanceof PbTypeName) {
      if (((PbTypeName) container).isBuiltInType()) {
        return false;
      }
    }
    switch (getReferenceState(path.getReference())) {
      case AMBIGUOUS -> {
        if (path.getParent() instanceof ProtoSymbolPath) {
          return false;
        }
        holder.newAnnotation(
            HighlightSeverity.ERROR, PbLangBundle.message("ambiguous.symbol", path.getSymbol().getText()))
          .range(path.getSymbol().getTextRange())
          .create();
        return true;
      }
      case VALID, NULL -> {
      }
      default -> {
        holder.newAnnotation(
            HighlightSeverity.ERROR, PbLangBundle.message("cannot.resolve.symbol", path.getSymbol().getText()))
          .range(path.getSymbol().getTextRange())
          .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
          .withFix(new PbAddImportStatementIntention())
          .create();
        return true;
      }
    }
    return false;
  }

  static void annotateExtensionName(
      @Nullable ProtoSymbolPath symbolPath,
      @Nullable PsiReference ref,
      @NotNull String referenceString,
      @Nullable PbNamedTypeElement qualifierType,
      @NotNull AnnotationHolder holder,
      boolean allowMembers) {
    if (symbolPath == null) {
      return;
    }
    if (ref == null) {
      return;
    }
    TextRange symbolRange = symbolPath.getSymbol().getTextRange();
    PsiElement resolved = ref.resolve();
    if (resolved == null) {
      // Unresolved refs should be handled by symbol path annotation.
      return;
    }

    if (resolved instanceof PbField) {
      // This could happen if the descriptor is missing.
      if (qualifierType == null) {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("unresolved.parent.type"))
            .range(symbolRange)
            .create();
      } else {
        PbField field = (PbField) resolved;
        boolean isExtension =
            allowMembers
                ? PbPsiUtil.fieldIsExtensionOrMember(field, qualifierType)
                : PbPsiUtil.fieldIsExtension(field, qualifierType);
        if (!isExtension) {
          holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message(
              "field.does.not.extend.type", field.getName(), qualifierType.getQualifiedName()))
              .range(symbolRange)
              .create();
        }
      }

    } else {
      // Extension option name must point to a field.
      holder.newAnnotation(
          HighlightSeverity.ERROR, PbLangBundle.message("extension.option.not.a.field", referenceString))
          .range(symbolRange)
          .create();
    }
  }
}
