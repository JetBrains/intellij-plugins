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
package com.intellij.protobuf.ide.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.ProtoNumberValue.SourceType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/** A small highlighting annotator for text format elements. */
public class PbTextHighlightingAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    element.accept(
        new PbTextVisitor() {
          @Override
          public void visitIdentifierValue(@NotNull PbTextIdentifierValue identifier) {
            PsiReference ref = identifier.getReference();
            // First, check to see if this is an enum value.
            if (ref != null) {
              // A non-null reference means this could be an enum value, if it resolves.
              if (ref.resolve() != null) {
                setHighlighting(identifier, holder, getEnumValueKey(identifier));
              }
            } else {
              // No reference - check to see if it should be highlighted as a special built-in
              // value
              ProtoNumberValue numberValue = identifier.getAsNumber();
              if (numberValue != null) {
                visitNumberValue(numberValue);
              } else if (identifier.getBooleanValue() != null) {
                setHighlighting(identifier, holder, getKeywordKey(identifier));
              }
            }
          }

          @Override
          public void visitNumberValue(@NotNull PbTextNumberValue number) {
            visitNumberValue((ProtoNumberValue) number);
          }

          void visitNumberValue(@NotNull ProtoNumberValue number) {
            SourceType sourceType = number.getSourceType();
            if (sourceType == SourceType.INF || sourceType == SourceType.NAN) {
              PsiElement numberElement = number.getNumberElement();
              if (numberElement != null) {
                setHighlighting(numberElement, holder, getKeywordKey(number));
              }
            }
          }
        });
  }

  private static TextAttributesKey getKeywordKey(PsiElement element) {
    if (element.getContainingFile() instanceof PbFile) {
      return PbSyntaxHighlighter.KEYWORD;
    } else {
      return PbTextSyntaxHighlighter.KEYWORD;
    }
  }

  private static TextAttributesKey getEnumValueKey(PsiElement element) {
    if (element.getContainingFile() instanceof PbFile) {
      return PbSyntaxHighlighter.ENUM_VALUE;
    } else {
      return PbTextSyntaxHighlighter.ENUM_VALUE;
    }
  }

  private static void setHighlighting(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder,
      @NotNull TextAttributesKey key) {
    holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(element.getTextRange())
        .textAttributes(key)
        .create();
  }
}
