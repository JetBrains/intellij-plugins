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
package com.intellij.protobuf.ide.editing;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * Support for (un)commenting sets of lines via IntelliJ. Being "CodeDocumentationAware" also
 * handles hitting enter in the middle of a comment more elegantly.
 */
public class PbCommenter implements CodeDocumentationAwareCommenter {
  public static final PbCommenter INSTANCE = new PbCommenter();

  @Override
  public @Nullable IElementType getLineCommentTokenType() {
    return ProtoTokenTypes.LINE_COMMENT;
  }

  @Override
  public @Nullable IElementType getBlockCommentTokenType() {
    return ProtoTokenTypes.BLOCK_COMMENT;
  }

  @Override
  public @Nullable IElementType getDocumentationCommentTokenType() {
    return null;
  }

  @Override
  public @Nullable String getDocumentationCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getDocumentationCommentLinePrefix() {
    return null;
  }

  @Override
  public @Nullable String getDocumentationCommentSuffix() {
    return null;
  }

  @Override
  public boolean isDocumentationComment(PsiComment psiComment) {
    return false;
  }

  @Override
  public @Nullable String getLineCommentPrefix() {
    return "//";
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    return "/*";
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    return "*/";
  }

  @Override
  public @Nullable String getCommentedBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentSuffix() {
    return null;
  }
}
