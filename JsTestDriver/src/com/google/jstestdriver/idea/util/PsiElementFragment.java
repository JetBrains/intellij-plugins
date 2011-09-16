package com.google.jstestdriver.idea.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class PsiElementFragment<T extends PsiElement> {

  private final T myElement;
  private final TextRange myTextRangeInElement;

  public PsiElementFragment(@NotNull T element, @NotNull TextRange textRangeInElement) {
    if (textRangeInElement.getStartOffset() < 0 || textRangeInElement.getEndOffset() < textRangeInElement.getStartOffset()) {
      throw new RuntimeException("TextRange " + textRangeInElement + " is not valid!");
    }
    if (element.getTextLength() < textRangeInElement.getEndOffset()) {
      throw new RuntimeException("TextRange " + textRangeInElement + " is out of '" + element.getText() + "'!");
    }
    myElement = element;
    myTextRangeInElement = textRangeInElement;
  }

  @NotNull
  public T getElement() {
    return myElement;
  }

  @NotNull
  public TextRange getTextRangeInElement() {
    return myTextRangeInElement;
  }

  @NotNull
  public TextRange getDocumentTextRange() {
    return myTextRangeInElement.shiftRight(myElement.getTextRange().getStartOffset());
  }

  @NotNull
  public String getText() {
    return myTextRangeInElement.substring(myElement.getText());
  }

  public <P extends PsiElement> PsiElementFragment<P> getSameTextRangeForParent(P parent) {
    int shift = myElement.getTextRange().getStartOffset() - parent.getTextRange().getStartOffset();
    return new PsiElementFragment<P>(parent, TextRange.create(myTextRangeInElement.getStartOffset() + shift, myTextRangeInElement.getEndOffset() + shift));
  }

  @Nullable
  public DocumentFragment toDocumentFragment() {
    Document document = JsPsiUtils.getDocument(myElement);
    if (document == null) {
      return null;
    }
    int startElementOffset = myElement.getTextRange().getStartOffset();
    TextRange documentTextRange = myTextRangeInElement.shiftRight(startElementOffset);
    return new DocumentFragment(document, documentTextRange.getStartOffset(), documentTextRange.getEndOffset());
  }

  public static <T extends PsiElement> PsiElementFragment<T> create(@NotNull T element, @NotNull TextRange textRangeInElement) {
    return new PsiElementFragment<T>(element, textRangeInElement);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PsiElementFragment that = (PsiElementFragment)o;

    return myElement.equals(that.myElement) && myTextRangeInElement.equals(that.myTextRangeInElement);
  }

  @Override
  public int hashCode() {
    int result = myElement.hashCode();
    result = 31 * result + myTextRangeInElement.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "PsiElementFragment{element=" + myElement +
           ", textRangeInElement=" + myTextRangeInElement +
           ", result='" + getText() + "'" +
           "}";
  }
}
