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
import com.intellij.protobuf.lang.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** Annotations specific to editions >= 2023. */
public class EditionsAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    // Only operate on editions files.
    if (!(element instanceof PbElement pbElement)
        || !(pbElement.getPbFile().getSyntaxLevel() instanceof SyntaxLevel.Edition)) {
      return;
    }

    element.accept(
        new PbVisitor() {
          @Override
          public void visitSyntaxStatement(@NotNull PbSyntaxStatement syntax) {
            annotateEdition(syntax, holder);
          }

          @Override
          public void visitField(@NotNull PbField field) {
            annotateField(field, holder);
          }

          @Override
          public void visitGroupDefinition(@NotNull PbGroupDefinition group) {
            annotateGroupDefinition(group, holder);
          }
        });
  }

  /*
   * Check the edition specification.
   */
  private static void annotateEdition(PbSyntaxStatement syntax, AnnotationHolder holder) {
    SyntaxLevel syntaxLevel = syntax.getSyntaxLevel();
    var effectiveSyntaxVersion = syntaxLevel == null ? "" : syntaxLevel.getVersion();
    if (!"2023".equals(effectiveSyntaxVersion)) {
      holder
          .newAnnotation(
              HighlightSeverity.ERROR,
              PbLangBundle.message("editions.unsupported", effectiveSyntaxVersion))
          .range(syntax)
          .create();
    }
  }

  /*
   * In editions, only repeated or null labels are allowed.
   */
  private static void annotateField(PbField field, AnnotationHolder holder) {
    if (field instanceof PbMapField) {
      return;
    }
    PbFieldLabel label = field.getDeclaredLabel();
    if (label != null && !label.getText().equals("repeated")) {
      holder
          .newAnnotation(
              HighlightSeverity.ERROR,
              PbLangBundle.message("editions.field.label." + label.getText()))
          .range(label)
          .create();
    }
  }

  /*
   * Group syntax is not allowed
   */
  private static void annotateGroupDefinition(PbGroupDefinition group, AnnotationHolder holder) {
    holder
        .newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("editions.group.invalid"))
        .range(group)
        .create();
  }
}
