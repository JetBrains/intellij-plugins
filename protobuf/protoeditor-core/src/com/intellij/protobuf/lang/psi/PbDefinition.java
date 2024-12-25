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

import com.intellij.protobuf.lang.psi.util.PbCommentUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** A definition is a container element, such as a message, enum, or service. */
public interface PbDefinition extends PbElement, PbStatement, PbStatementOwner {

  /** Returns the definition body: a container wrapped in braces containing child statements. */
  @Nullable
  PbBlockBody getBody();

  /** Returns trailing comments for this definition, which follow the body's opening brace. */
  @Override
  default @NotNull List<PsiComment> getTrailingComments() {
    PbBlockBody body = getBody();
    if (body == null) {
      return Collections.emptyList();
    }
    return PbCommentUtil.collectTrailingComments(body.getStart());
  }
}
