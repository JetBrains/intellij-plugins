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

import com.google.common.base.Ascii;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;

/** Annotations specific to proto2 syntax level. */
public class Proto2Annotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {
    // Only operate on proto2 files.
    if (!(element instanceof PbElement)
        || ((PbElement) element).getPbFile().getSyntaxLevel() != SyntaxLevel.PROTO2) {
      return;
    }

    element.accept(
        new PbVisitor() {
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
   * In proto2, fields must have labels unless they're map fields or are part of oneof definitions.
   */
  private static void annotateField(PbField field, AnnotationHolder holder) {
    if (field instanceof PbMapField) {
      return;
    }
    if (field.getStatementOwner() instanceof PbOneofDefinition) {
      return;
    }
    if (field.getDeclaredLabel() == null) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto2.field.label.required"))
          .range(field)
          .create();
    }
  }

  /*
   * Group fields must have labels.
   * Group field names must start with a capital letter.
   */
  private static void annotateGroupDefinition(PbGroupDefinition group, AnnotationHolder holder) {
    PsiElement nameIdentifier = group.getNameIdentifier();
    String name = group.getName();
    if (name != null && nameIdentifier != null && !Ascii.isUpperCase(name.charAt(0))) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto2.group.name.capital.letter"))
          .range(nameIdentifier)
          .create();
    }
    if (!(group.getStatementOwner() instanceof PbOneofDefinition)
        && group.getDeclaredLabel() == null) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto2.field.label.required"))
          .range(group)
          .create();
    }
  }
}
