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
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.PsiComment;
import com.intellij.protobuf.lang.psi.util.PbCommentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** An element that can return leading and trailing comments. */
public interface PbCommentOwner extends PbElement {

  /** Returns comments that precede this element. */
  @NotNull
  default List<PsiComment> getLeadingComments() {
    return PbCommentUtil.collectLeadingComments(this);
  }

  /**
   * Returns comments that follow this element.
   *
   * <p>For elements with bodies (such as messages), trailing comments follow the opening brace. For
   * example:
   *
   * <pre>
   *   message Foo {  // This is a trailing comment.
   *   }
   * </pre>
   */
  @NotNull
  default List<PsiComment> getTrailingComments() {
    return PbCommentUtil.collectTrailingComments(this);
  }

  /** Returns leading and trailing comments combined. */
  @NotNull
  default List<PsiComment> getComments() {
    List<PsiComment> leadingComments = getLeadingComments();
    List<PsiComment> trailingComments = getTrailingComments();
    if (leadingComments.isEmpty()) {
      return trailingComments;
    } else if (trailingComments.isEmpty()) {
      return leadingComments;
    } else {
      List<PsiComment> comments = new ArrayList<>(leadingComments.size() + trailingComments.size());
      comments.addAll(leadingComments);
      comments.addAll(trailingComments);
      return comments;
    }
  }
}
