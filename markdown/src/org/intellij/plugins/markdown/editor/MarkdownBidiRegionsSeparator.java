package org.intellij.plugins.markdown.editor;

import com.intellij.openapi.editor.bidi.TokenSetBidiRegionsSeparator;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

public class MarkdownBidiRegionsSeparator extends TokenSetBidiRegionsSeparator {
  public MarkdownBidiRegionsSeparator() {
    super(TokenSet.create(MarkdownTokenTypes.TEXT, MarkdownTokenTypes.WHITE_SPACE));
  }
}
