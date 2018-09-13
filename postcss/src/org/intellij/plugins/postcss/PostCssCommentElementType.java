package org.intellij.plugins.postcss;

import com.intellij.psi.css.impl.CssCommentElementType;
import org.jetbrains.annotations.NotNull;

public class PostCssCommentElementType extends PostCssElementType implements CssCommentElementType {
  public PostCssCommentElementType(String debugName) {
    super(debugName);
  }

  @Override
  public int getStartDelta() {
    return 2;
  }

  @Override
  public int getEndDelta() {
    return 0;
  }

  @NotNull
  @Override
  public String getContinuationChars() {
    return "";
  }
}
