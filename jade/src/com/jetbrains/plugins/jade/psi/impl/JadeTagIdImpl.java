package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class JadeTagIdImpl extends LeafPsiElement {
  public JadeTagIdImpl(@NotNull IElementType type, CharSequence text) {
    super(type, text);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }

  public static final class Manipulator extends AbstractElementManipulator<JadeTagIdImpl> {
    @Override
    public JadeTagIdImpl handleContentChange(@NotNull JadeTagIdImpl element, @NotNull TextRange range, String newContent) throws
                                                                                                                          IncorrectOperationException {
      if (range.getLength() != element.getTextLength() - 1) {
        return null;
      }

      return ((JadeTagIdImpl)element.replaceWithText("#" + newContent));
    }
  }
}
