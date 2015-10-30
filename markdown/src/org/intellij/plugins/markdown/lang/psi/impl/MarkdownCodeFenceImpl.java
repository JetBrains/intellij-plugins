package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class MarkdownCodeFenceImpl extends CompositePsiElement implements PsiLanguageInjectionHost {
  public MarkdownCodeFenceImpl(IElementType type) {
    super(type);
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @NotNull
  @Override
  public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return new LiteralTextEscaper<PsiLanguageInjectionHost>(this) {
      @Override
      public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
        outChars.append(rangeInsideHost.substring(myHost.getText()));
        return true;
      }

      @Override
      public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
        return rangeInsideHost.getStartOffset() + offsetInDecoded;
      }

      @NotNull
      @Override
      public TextRange getRelevantTextRange() {
        final MarkdownCodeFenceContentImpl first = PsiTreeUtil.findChildOfType(myHost, MarkdownCodeFenceContentImpl.class);
        if (first == null) {
          return TextRange.EMPTY_RANGE;
        }

        MarkdownCodeFenceContentImpl last = null;
        for (PsiElement child = myHost.getLastChild(); child != null; child = child.getPrevSibling()) {
          if (child instanceof MarkdownCodeFenceContentImpl) {
            last = ((MarkdownCodeFenceContentImpl)child);
            break;
          }
        }
        assert last != null;

        return TextRange.create(first.getStartOffsetInParent(), last.getStartOffsetInParent() + last.getTextLength());
      }

      @Override
      public boolean isOneLine() {
        return false;
      }
    };
  }

  public static class Manipulator extends AbstractElementManipulator<MarkdownCodeFenceImpl> {

    @Override
    public MarkdownCodeFenceImpl handleContentChange(@NotNull MarkdownCodeFenceImpl element, @NotNull TextRange range, String newContent)
      throws IncorrectOperationException {
      return null;
    }
  }
}
