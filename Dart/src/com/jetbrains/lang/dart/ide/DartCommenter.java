package com.jetbrains.lang.dart.ide;

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 10/13/11
 * Time: 11:50 AM
 */
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
    return element.getTokenType() == DartTokenTypesSets.DOC_COMMENT;
  }

  @Nullable
  public IElementType getDocumentationCommentTokenType() {
    return DartTokenTypesSets.DOC_COMMENT;
  }
}
