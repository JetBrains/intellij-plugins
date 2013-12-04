package com.jetbrains.lang.dart.ide;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import org.jetbrains.annotations.Nullable;

public class DartCommenter implements CodeDocumentationAwareCommenter {
  public String getLineCommentPrefix() {
    return "//";
  }

  public String getBlockCommentPrefix() {
    return "/*";
  }

  public String getBlockCommentSuffix() {
    return "*/";
  }

  public String getCommentedBlockCommentPrefix() {
    return null;
  }

  public String getCommentedBlockCommentSuffix() {
    return null;
  }

  @Nullable
  public IElementType getLineCommentTokenType() {
    return DartTokenTypesSets.SINGLE_LINE_COMMENT;
  }

  @Nullable
  public IElementType getBlockCommentTokenType() {
    return DartTokenTypesSets.MULTI_LINE_COMMENT;
  }

  public String getDocumentationCommentPrefix() {
    return "/**";
  }

  public String getDocumentationCommentLinePrefix() {
    return "*";
  }

  public String getDocumentationCommentSuffix() {
    return "*/";
  }

  public boolean isDocumentationComment(final PsiComment element) {
    return element.getTokenType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT ||
           element.getTokenType() == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT;
  }

  @Nullable
  public IElementType getDocumentationCommentTokenType() {
    return DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT;
  }
}
