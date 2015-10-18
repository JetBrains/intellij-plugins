package org.intellij.plugins.markdown.ui.actions;

import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.jetbrains.annotations.NotNull;

public class ToggleItalicAction extends BaseToggleStateAction {
  @NotNull
  protected String getBoundString(boolean isWord) {
    return isWord ? "_" : "*";
  }

  protected boolean shouldMoveToWordBounds() {
    return true;
  }

  @NotNull
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.EMPH;
  }
}
