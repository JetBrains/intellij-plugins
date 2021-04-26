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
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.protobuf.ide.highlighter.PbTextSyntaxHighlighter;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.protobuf.lang.resolve.PbImportReference;
import com.intellij.protobuf.lang.resolve.directive.SchemaComment.Type;
import org.jetbrains.annotations.NotNull;

/** An annotator that highlights directive comments and marks unresolved symbols with warnings. */
public class SchemaDirectiveAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof PsiComment && element.getContainingFile() instanceof PbTextFile) {
      annotateDirectiveComment((PsiComment) element, holder);
    }
  }

  private void annotateDirectiveComment(PsiComment comment, AnnotationHolder holder) {
    SchemaDirective directive = SchemaDirective.find(comment.getContainingFile());
    if (directive == null) {
      return;
    }

    SchemaComment schemaComment = directive.getSchemaComment(comment);
    if (schemaComment == null) {
      return;
    }

    TextRange keyRange = schemaComment.getKeyRange();
    if (keyRange == null) {
      return;
    }
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(keyRange.shiftRight(comment.getTextOffset()))
        .textAttributes(PbTextSyntaxHighlighter.COMMENT_DIRECTIVE)
        .create();

    if (schemaComment.getName() == null) {
       holder.newAnnotation(HighlightSeverity.WARNING, missingNameWarning(schemaComment.getType()))
          .range(keyRange.shiftRight(comment.getTextOffset()))
          .afterEndOfLine()
          .create();
      return;
    }

    // Make sure the specified message name is actually a message.
    if (schemaComment.getType() == Type.MESSAGE) {
      PsiReference messageReference = schemaComment.getReference();
      if (messageReference != null) {
        PsiElement resolved = messageReference.resolve();
        if (resolved != null && !(resolved instanceof PbMessageType)) {
          holder.newAnnotation(HighlightSeverity.WARNING, PbLangBundle.message("message.type.expected"))
              .range(messageReference.getRangeInElement().shiftRight(comment.getTextOffset()))
              .create();
        }
      }
    }

    // Add warning to comments if one of the message or file comments is missing.
    if (directive.getFileComment() == null || directive.getMessageComment() == null) {
      holder.newAnnotation(
          HighlightSeverity.WARNING, PbLangBundle.message("file.and.message.comments.must.be.specified"))
          .range(keyRange.shiftRight(comment.getTextOffset()))
          .create();
    }

    for (PsiReference ref : schemaComment.getAllReferences()) {
      String symbol = ref.getRangeInElement().substring(ref.getElement().getText());
      if (symbol.isEmpty() || ref.resolve() != null) {
        continue;
      }
      holder.newAnnotation(HighlightSeverity.WARNING, cannotResolveWarning(ref, symbol))
          .range(ref.getRangeInElement().shiftRight(ref.getElement().getStartOffsetInParent()))
          .create();
    }
  }

  @NotNull
  private String missingNameWarning(SchemaComment.Type type) {
    switch (type) {
      case FILE:
      case IMPORT:
        return PbLangBundle.message("missing.filename");
      case MESSAGE:
        return PbLangBundle.message("missing.message.name");
      default:
        throw new AssertionError("Unknown SchemaComment.Type: " + type);
    }
  }

  private String cannotResolveWarning(PsiReference ref, String name) {
    if (ref instanceof PbImportReference) {
      return PbLangBundle.message("cannot.resolve.import", name);
    }
    return PbLangBundle.message("cannot.resolve.symbol", name);
  }
}
