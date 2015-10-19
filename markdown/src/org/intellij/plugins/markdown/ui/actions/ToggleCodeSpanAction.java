package org.intellij.plugins.markdown.ui.actions;

import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleCodeSpanAction extends BaseToggleStateAction {
  @NotNull
  @Override
  protected String getBoundString(boolean isWord) {
    return "`";
  }

  @Nullable
  @Override
  protected String getExistingBoundString(@NotNull CharSequence text, int startOffset) {
    int to = startOffset;
    while (to < text.length() && text.charAt(to) == '`') {
      to++;
    }

    return text.subSequence(startOffset, to).toString();
  }

  @Override
  protected boolean shouldMoveToWordBounds() {
    return false;
  }

  @NotNull
  @Override
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.CODE_SPAN;
  }
}
