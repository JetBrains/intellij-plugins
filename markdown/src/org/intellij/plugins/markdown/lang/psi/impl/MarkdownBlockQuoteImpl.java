package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.DelegatingItemPresentation;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;

public class MarkdownBlockQuoteImpl extends MarkdownCompositePsiElementBase {
  public MarkdownBlockQuoteImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "blockquote";
  }

  @Override
  public ItemPresentation getPresentation() {
    return new DelegatingItemPresentation(super.getPresentation()) {
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }
        if (hasTrivialChildren()) {
          return super.getLocationString();
        }
        else {
          return null;
        }
      }
    };
  }
}
