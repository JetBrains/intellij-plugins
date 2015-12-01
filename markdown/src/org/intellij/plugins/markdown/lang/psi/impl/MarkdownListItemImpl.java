package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MarkdownListItemImpl extends MarkdownCompositePsiElementBase {
  public MarkdownListItemImpl(@NotNull ASTNode node) {
    super(node);
  }

  public PsiElement getMarkerElement() {
    final PsiElement child = getFirstChild();
    if (child != null && MarkdownTokenTypeSets.LIST_MARKERS.contains(child.getNode().getElementType())) {
      return child;
    }
    else {
      return null;
    }
  }
  
  @Override
  public ItemPresentation getPresentation() {
    return new MarkdownBasePresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        if (!isValid()) {
          return null;
        }
        return getMarkerElement().getText();
      }

      @Nullable
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }
        
        if (hasTrivialChildren()) {
          final MarkdownCompositePsiElementBase element = findChildByClass(MarkdownCompositePsiElementBase.class);
          assert element != null;
          return element.shrinkTextTo(PRESENTABLE_TEXT_LENGTH);
        }
        else {
          return null;
        }
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return null;
      }
    };
  }

  @Override
  protected String getPresentableTagName() {
    return "li";
  }
}
