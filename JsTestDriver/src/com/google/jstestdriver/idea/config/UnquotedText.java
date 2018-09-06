package com.google.jstestdriver.idea.config;

import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
class UnquotedText {

  private final PsiElement myPsiElement;
  private final TextRange myUnquotedDocumentTextRange;

  UnquotedText(@NotNull PsiElement psiElement) {
    myPsiElement = psiElement;
    myUnquotedDocumentTextRange = calcUnquotedDocumentTextRange(psiElement);
  }

  @NotNull
  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  @NotNull
  public String getUnquotedText() {
    int base = myPsiElement.getTextRange().getStartOffset();
    int start = myUnquotedDocumentTextRange.getStartOffset() - base;
    int end = myUnquotedDocumentTextRange.getEndOffset() - base;
    String text = myPsiElement.getText();
    return text.substring(start, end);
  }

  @NotNull
  public TextRange getUnquotedDocumentTextRange() {
    return myUnquotedDocumentTextRange;
  }

  public static DocumentFragment unquoteDocumentFragment(@NotNull DocumentFragment fragment) {
    String str = fragment.getDocument().getText(fragment.getTextRange());
    TextRange unquoted = calcUnquotedDocumentTextRange(str, fragment.getTextRange());
    return new DocumentFragment(fragment.getDocument(), unquoted.getStartOffset(), unquoted.getEndOffset());
  }

  private static TextRange calcUnquotedDocumentTextRange(@NotNull PsiElement element) {
    return calcUnquotedDocumentTextRange(element.getText(), element.getTextRange());
  }

  private static TextRange calcUnquotedDocumentTextRange(@NotNull String str, @NotNull TextRange textRange) {
    String unquotedStr = StringUtil.unquoteString(str);
    boolean quoted = !str.equals(unquotedStr);
    int startOffset = textRange.getStartOffset();
    if (quoted) {
      startOffset++;
    }
    int endOffset = textRange.getEndOffset();
    if (quoted) {
      endOffset--;
    }
    return new TextRange(startOffset, endOffset);
  }

  @Override
  public String toString() {
    return getUnquotedText();
  }
}
