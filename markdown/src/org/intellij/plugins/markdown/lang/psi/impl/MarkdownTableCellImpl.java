package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class MarkdownTableCellImpl extends MarkdownCompositePsiElementBase {
  public MarkdownTableCellImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "td";
  }
}
