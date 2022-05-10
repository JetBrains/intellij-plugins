package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinCommenter implements CodeDocumentationAwareCommenter {
  @NonNls
  private static final String LINE_COMMENT_PREFIX = "#";

  @Override
  @Nullable
  public String getLineCommentPrefix() {
    return LINE_COMMENT_PREFIX;
  }

  @Override
  @Nullable
  public String getBlockCommentPrefix() {
    // N/A
    return null;
  }

  @Override
  @Nullable
  public String getBlockCommentSuffix() {
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

  @Nullable
  @Override
  public IElementType getLineCommentTokenType() {
    return GherkinTokenTypes.COMMENT;
  }

  @Nullable
  @Override
  public IElementType getBlockCommentTokenType() {
    return null;
  }

  @Nullable
  @Override
  public IElementType getDocumentationCommentTokenType() {
    return null;
  }

  @Nullable
  @Override
  public String getDocumentationCommentPrefix() {
    return null;
  }

  @Nullable
  @Override
  public String getDocumentationCommentLinePrefix() {
    return null;
  }

  @Nullable
  @Override
  public String getDocumentationCommentSuffix() {
    return null;
  }

  @Override
  public boolean isDocumentationComment(PsiComment element) {
    return false;
  }
}