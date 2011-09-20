package com.google.jstestdriver.idea.config;

import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class QuotedText {

  private final PsiElement myPsiElement;
  private final TextRange myUsefulDocumentTextRange;

  public QuotedText(@NotNull PsiElement psiElement) {
    myPsiElement = psiElement;
    myUsefulDocumentTextRange = calcUsefulDocumentTextRange(psiElement);
  }

  @NotNull
  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  @NotNull
  public String getUsefulText() {
    int base = myPsiElement.getTextRange().getStartOffset();
    int start = myUsefulDocumentTextRange.getStartOffset() - base;
    int end = myUsefulDocumentTextRange.getEndOffset() - base;
    String text = myPsiElement.getText();
    return text.substring(start, end);
  }

  @NotNull
  public TextRange getUsefulDocumentTextRange() {
    return myUsefulDocumentTextRange;
  }

  public static DocumentFragment unquoteDocumentFragment(@NotNull DocumentFragment fragment) {
    String str = fragment.getDocument().getText(fragment.getTextRange());
    TextRange usefulRange = calcUsefulDocumentTextRange(str, fragment.getTextRange());
    return new DocumentFragment(fragment.getDocument(), usefulRange.getStartOffset(), usefulRange.getEndOffset());
  }

  private static TextRange calcUsefulDocumentTextRange(@NotNull PsiElement element) {
    return calcUsefulDocumentTextRange(element.getText(), element.getTextRange());
  }

  private static TextRange calcUsefulDocumentTextRange(@NotNull String str, @NotNull TextRange textRange) {
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

}
