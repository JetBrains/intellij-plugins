package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.DelegatingItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.jetbrains.annotations.NotNull;

public class MarkdownHeaderImpl extends MarkdownCompositePsiElementBase {
  public MarkdownHeaderImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "h" + getHeaderNumber();
  }

  @Override
  public ItemPresentation getPresentation() {
    return new DelegatingItemPresentation(super.getPresentation()) {
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }
        final PsiElement contentHolder = findChildByType(MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES);
        if (contentHolder == null) {
          return null;
        }
        else {
          return contentHolder.getText();
        }
      }
    };
  }

  private int getHeaderNumber() {
    final IElementType type = getNode().getElementType();
    if (MarkdownTokenTypeSets.HEADER_LEVEL_1_SET.contains(type)) {
      return 1;
    }
    if (MarkdownTokenTypeSets.HEADER_LEVEL_2_SET.contains(type)) {
      return 2;
    }
    if (type == MarkdownElementTypes.ATX_3) {
      return 3;
    }
    if (type == MarkdownElementTypes.ATX_4) {
      return 4;
    }
    if (type == MarkdownElementTypes.ATX_5) {
      return 5;
    }
    if (type == MarkdownElementTypes.ATX_6) {
      return 6;
    }
    throw new IllegalStateException("Type should be one of header types");
  }
}
