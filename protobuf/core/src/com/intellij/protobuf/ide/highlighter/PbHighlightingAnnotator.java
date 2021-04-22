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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.ProtoNumberValue.SourceType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An annotator that syntax-highlights keywords.
 *
 * <p>Since the protobuf language allows keywords to be used as identifier names, highlighting must
 * be done after parsing to be correct. In the following example, the first "message" should be
 * highlighted as a keyword, but the second should not as it is the name of the message type.
 *
 * <pre>
 *   message message {
 *     required int32 x = 1;
 *   }
 * </pre>
 */
public class PbHighlightingAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {

    element.accept(
        new PbVisitor() {

          @Override
          public void visitIdentifierValue(@NotNull PbIdentifierValue value) {
            PsiReference ref = value.getReference();
            // First, check to see if this is an enum value.
            if (ref != null) {
              // A non-null reference means this could be an enum value, if it resolves.
              if (ref.resolve() != null) {
                setHighlighting(value, holder, PbSyntaxHighlighter.ENUM_VALUE);
              }
            } else {
              // No reference - check to see if it should be highlighted as a special built-in value
              ProtoNumberValue numberValue = value.getAsNumber();
              if (numberValue != null) {
                visitNumberValue(numberValue);
              } else if (value.getBooleanValue() != null) {
                setHighlighting(value, holder, PbSyntaxHighlighter.KEYWORD);
              }
            }
          }

          @Override
          public void visitNumberValue(@NotNull PbNumberValue number) {
            visitNumberValue((ProtoNumberValue) number);
          }

          void visitNumberValue(@NotNull ProtoNumberValue number) {
            SourceType sourceType = number.getSourceType();
            if (sourceType == SourceType.INF || sourceType == SourceType.NAN) {
              PsiElement numberElement = number.getNumberElement();
              if (numberElement != null) {
                setHighlighting(numberElement, holder, PbSyntaxHighlighter.KEYWORD);
              }
            }
          }

          @Override
          public void visitOptionName(@NotNull PbOptionName name) {
            if (name.isSpecial()) {
              setHighlighting(name, holder, PbSyntaxHighlighter.KEYWORD);
            }
          }

          @Override
          public void visitTypeName(@NotNull PbTypeName type) {
            if (type.isBuiltInType()) {
              setHighlighting(type, holder, PbSyntaxHighlighter.KEYWORD);
            }
          }

          @Override
          public void visitElement(@NotNull PsiElement element) {
            IElementType type = element.getNode().getElementType();
            if (Objects.equals(
                PbSyntaxHighlighter.getTokenKey(type), PbSyntaxHighlighter.KEYWORD)) {
              setHighlighting(element, holder, PbSyntaxHighlighter.KEYWORD);
            } else if (ProtoTokenTypes.IDENTIFIER_LITERAL.equals(type)) {
              PsiElement parent = element.getParent();
              if (parent instanceof PsiNameIdentifierOwner
                  && element.equals(((PsiNameIdentifierOwner) parent).getNameIdentifier())) {
                highlightNameIdentifier((PsiNameIdentifierOwner) parent, element, holder);
              }
            }
          }
        });
  }

  private void highlightNameIdentifier(
      PsiNameIdentifierOwner parent, PsiElement name, AnnotationHolder holder) {
    if (parent instanceof PbEnumValue) {
      setHighlighting(name, holder, PbSyntaxHighlighter.ENUM_VALUE);
    }
  }

  private void setHighlighting(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder,
      @NotNull TextAttributesKey key) {
    holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(element.getTextRange())
        .textAttributes(key)
        .create();
  }
}
