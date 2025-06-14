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
import com.intellij.protobuf.lang.psi.PbOptionExpression;
import com.intellij.protobuf.lang.psi.PbOptionName;
import com.intellij.protobuf.lang.psi.PbVisitor;
import com.intellij.protobuf.lang.psi.SyntaxLevel;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/** Annotations specific to editions >= 2024. */
public class Edition2024Annotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    // Only operate on editions files >= edition 2024.

    if (!(element instanceof PbElement pbElement)
        || !(pbElement.getPbFile().getSyntaxLevel() instanceof SyntaxLevel.Edition)
        || pbElement.getPbFile().getSyntaxLevel().getVersion().compareTo("2024") < 0) {
      return;
    }

    element.accept(
        new PbVisitor() {

          @Override
          public void visitImportStatement(@NotNull PbImportStatement statement) {
            annotateImportStatement(statement, holder);
          }

          @Override
          public void visitOptionName(@NotNull PbOptionName name) {
            annotateOptionName(name, holder);
          }
        });
  }

  /*
   * Weak imports are not allowed.
   */
  private static void annotateImportStatement(
      PbImportStatement statement, AnnotationHolder holder) {
    PsiElement label = statement.getImportLabel();
    if (label != null && statement.isWeak()) {
      holder
          .newAnnotation(
              HighlightSeverity.ERROR, PbLangBundle.message("editions.2024.weak.imports"))
          .range(label)
          .create();
    }
  }

  /*
   * The 'weak' option is not supported.
   * The 'ctype' option is not supported.
   */
  private static void annotateOptionName(PbOptionName optionName, AnnotationHolder holder) {
    PbOptionExpression optionExpression =
        PsiTreeUtil.getParentOfType(optionName, PbOptionExpression.class);
    if (optionExpression == null) {
      return;
    }
    if (PbPsiUtil.isDescriptorOption(optionExpression, "weak")) {
      holder
          .newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("editions.2024.weak"))
          .range(optionName)
          .create();
    } else if (PbPsiUtil.isDescriptorOption(optionExpression, "ctype")) {
      holder
          .newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("editions.2024.ctype"))
          .range(optionName)
          .create();
    }
  }
}
