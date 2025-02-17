package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.generation.IndentedCommenter;
import org.jetbrains.annotations.Nullable;

public final class JadeCommenter implements IndentedCommenter {

  @Override
  public @Nullable String getLineCommentPrefix() {
    return "//";
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    return "//-";
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentSuffix() {
    return null;
  }

  @Override
  public @Nullable Boolean forceIndentedLineComment() {
    return true;
  }
}
