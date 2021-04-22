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

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * SchemaComment objects hold information about one of the PsiComment elements comprising a {@link
 * SchemaDirective}. Subclasses provide PsiReferences associated with the comment.
 */
public abstract class SchemaComment {

  /** The comment type: proto-file, proto-message, or proto-import. */
  public enum Type {
    FILE,
    MESSAGE,
    IMPORT
  }

  private final PsiComment comment;
  private final TextRange keyRange;
  private final TextRange nameRange;
  private final Type type;

  SchemaComment(@NotNull PsiComment comment, TextRange keyRange, TextRange nameRange, Type type) {
    this.comment = comment;
    this.keyRange = keyRange;
    this.nameRange = nameRange;
    this.type = type;
  }

  @NotNull
  public PsiComment getComment() {
    return comment;
  }

  @Nullable
  public TextRange getKeyRange() {
    return keyRange;
  }

  @Nullable
  public TextRange getNameRange() {
    return nameRange;
  }

  @Nullable
  public String getName() {
    return nameRange != null ? nameRange.substring(comment.getText()) : null;
  }

  public Type getType() {
    return type;
  }

  @Nullable
  public abstract PsiReference getReference();

  public abstract List<PsiReference> getAllReferences();

  @Override
  public int hashCode() {
    return Objects.hash(comment, keyRange, nameRange);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SchemaComment)) {
      return false;
    }
    SchemaComment otherComment = (SchemaComment) other;
    return Objects.equals(comment, otherComment.comment)
        && Objects.equals(keyRange, otherComment.keyRange)
        && Objects.equals(nameRange, otherComment.nameRange);
  }
}
