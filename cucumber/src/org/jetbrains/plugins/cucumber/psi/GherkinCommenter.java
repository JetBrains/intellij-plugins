// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinCommenter implements CodeDocumentationAwareCommenter {
  private static final @NonNls String LINE_COMMENT_PREFIX = "#";

  @Override
  public @Nullable String getLineCommentPrefix() {
    return LINE_COMMENT_PREFIX;
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    // N/A
    return null;
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    // N/A
    return null;
  }

  @Override
  public String getCommentedBlockCommentPrefix() {
    return null;
  }

  @Override
  public String getCommentedBlockCommentSuffix() {
    return null;
  }

  @Override
  public @Nullable IElementType getLineCommentTokenType() {
    return GherkinTokenTypes.COMMENT;
  }

  @Override
  public @Nullable IElementType getBlockCommentTokenType() {
    return null;
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
  public boolean isDocumentationComment(PsiComment element) {
    return false;
  }
}