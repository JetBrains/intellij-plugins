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

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.PbElement;
import com.intellij.protobuf.lang.psi.PbImportStatement;
import com.intellij.protobuf.lang.psi.PbSymbolVisibility;
import com.intellij.protobuf.lang.psi.PbVisitor;
import com.intellij.protobuf.lang.psi.SyntaxLevel;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** Annotations specific to editions < 2024. */
public class BeforeEdition2024Annotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    // Only operate on files before edition 2024, including proto2 and proto3.

    if (!(element instanceof PbElement pbElement)
        || (pbElement.getPbFile().getSyntaxLevel() instanceof SyntaxLevel.Edition
            && pbElement.getPbFile().getSyntaxLevel().getVersion().compareTo("2024") >= 0)) {
      return;
    }

    element.accept(
        new PbVisitor() {
          @Override
          public void visitSymbolVisibility(@NotNull PbSymbolVisibility symbolVisibility) {
            annotateSymbolVisibility(symbolVisibility, holder);
          }

          @Override
          public void visitImportStatement(@NotNull PbImportStatement statement) {
            annotateImportStatement(statement, holder);
          }
        });
  }

  /*
   * Symbol visibility is not allowed yet.
   */
  private static void annotateSymbolVisibility(
      PbSymbolVisibility symbolVisibility, AnnotationHolder holder) {
    if (symbolVisibility != null) {
      holder
          .newAnnotation(
              HighlightSeverity.ERROR, PbLangBundle.message("editions.2024.symbol.visibility"))
          .range(symbolVisibility)
          .create();
    }
  }

  /*
   * Option imports is not allowed yet.
   */
  private static void annotateImportStatement(
      PbImportStatement statement, AnnotationHolder holder) {
    PsiElement label = statement.getImportLabel();
    if (label != null && statement.isOption()) {
      holder
          .newAnnotation(
              HighlightSeverity.ERROR, PbLangBundle.message("editions.2024.option.imports"))
          .range(label)
          .create();
    }
  }
}
