package org.intellij.plugins.postcss;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

public class PostCssElementType extends IElementType {
  public PostCssElementType(@NonNls String debugName) {
    super(debugName, PostCssFileType.POST_CSS.getLanguage());
  }
}
