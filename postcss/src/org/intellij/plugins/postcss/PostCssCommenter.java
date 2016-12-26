package org.intellij.plugins.postcss;

import com.intellij.lang.Commenter;
import com.intellij.psi.css.impl.util.editor.CssCommenter;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class PostCssCommenter extends CssCommenter implements Commenter {
  @NonNls
  private static final String LINE_COMMENT_PREFIX = "//";

  @Nullable
  public String getLineCommentPrefix() {
    return LINE_COMMENT_PREFIX;
  }

  @Nullable
  @Override
  public IElementType getLineCommentTokenType() {
    return PostCssTokenTypes.POST_CSS_COMMENT;
  }
}