package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import org.jetbrains.annotations.NotNull;

public abstract class DartStringLiteralExpressionBase extends DartClassReferenceImpl implements DartStringLiteralExpression {
  private static final Logger LOG = Logger.getInstance(DartStringLiteralExpressionBase.class.getName());

  public DartStringLiteralExpressionBase(ASTNode node) {
    super(node);
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull final String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @NotNull
  @Override
  public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    // TODO
    return LiteralTextEscaper.createSimple(this);
  }

  public static class DartStringManipulator extends AbstractElementManipulator<DartStringLiteralExpression> {
    @Override
    public DartStringLiteralExpression handleContentChange(@NotNull final DartStringLiteralExpression oldElement,
                                                           @NotNull final TextRange range,
                                                           @NotNull final String newContent) {
      // this check helps to avoid loosing text in case of concatenated strings + typing escape sequences; need proper fix
      final int expectedNewLength = oldElement.getTextLength() - range.getLength() + newContent.length();

      final String newText = StringUtil.replaceSubstring(oldElement.getText(), range, newContent);
      final PsiFile fileFromText = PsiFileFactory.getInstance(oldElement.getProject())
        .createFileFromText(DartLanguage.INSTANCE, "var a = " + newText + ";");
      final PsiElement elementAt = fileFromText.findElementAt("var a = ".length());
      if (elementAt != null &&
          elementAt.getParent() instanceof DartStringLiteralExpression &&
          expectedNewLength == elementAt.getParent().getTextLength()) {
        return (DartStringLiteralExpression)oldElement.replace(elementAt.getParent());
      }

      return oldElement;
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull final DartStringLiteralExpression element) {
      // StringLiteralExpression may consist of several strings (that become concatenated). We want to return range of the first one. If none (e.g. "$a") - return zero-length range after quote
      PsiElement child = element.getFirstChild();
      while (child != null) {
        final IElementType type = child.getNode().getElementType();
        if (type == DartTokenTypes.OPEN_QUOTE) {
          final PsiElement next = child.getNextSibling();
          if (next == null || next.getNode().getElementType() != DartTokenTypes.REGULAR_STRING_PART) {
            return TextRange.from(child.getStartOffsetInParent() + child.getTextLength(), 0);
          }
        }

        if (type == DartTokenTypes.REGULAR_STRING_PART) {
          return child.getTextRange().shiftRight(-element.getTextRange().getStartOffset());
        }

        if (type == DartTokenTypes.RAW_SINGLE_QUOTED_STRING || type == DartTokenTypes.RAW_TRIPLE_QUOTED_STRING) {
          final TextRange textRange = DartPsiImplUtil.getUnquotedDartStringAndItsRange(child.getText()).second;
          return textRange.shiftRight(child.getStartOffsetInParent());
        }

        child = child.getNextSibling();
      }

      LOG.error(element.getText());
      return element.getTextRange();
    }
  }
}
