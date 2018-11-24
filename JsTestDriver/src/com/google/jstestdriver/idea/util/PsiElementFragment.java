package com.google.jstestdriver.idea.util;

import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class PsiElementFragment<T extends PsiElement> {

  private final T myElement;
  private final TextRange myTextRangeInElement;

  private PsiElementFragment(@NotNull T element, @NotNull TextRange textRangeInElement) {
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

  @NotNull
  public <P extends PsiElement> PsiElementFragment<P> getSameTextRangeForParent(P parent) {
    PsiElement element = myElement;
    while (element != parent) {
      element = element.getParent();
      if (element instanceof PsiFile) {
        break;
      }
    }
    if (element != parent) {
      throw new RuntimeException("Parent " + parent + " was not found for " + myElement);
    }
    int shift = myElement.getTextRange().getStartOffset() - parent.getTextRange().getStartOffset();
    return new PsiElementFragment<>(parent, myTextRangeInElement.shiftRight(shift));
  }

  @Nullable
  public DocumentFragment toDocumentFragment() {
    Document document = JsPsiUtils.getDocument(myElement);
    if (document == null) {
      return null;
    }
    TextRange documentTextRange = getDocumentTextRange();
    return new DocumentFragment(document, documentTextRange.getStartOffset(), documentTextRange.getEndOffset());
  }

  @Nullable
  public static <T extends PsiElement> PsiElementFragment<T> create(@NotNull T element,
                                                                    @NotNull DocumentFragment documentFragment) {
    Document document = JsPsiUtils.getDocument(element);
    if (document != documentFragment.getDocument()) {
      throw new RuntimeException("Documents are different: " + element
                                 + ", '" + element.getText() + "'");
    }
    TextRange dtr = documentFragment.getTextRange();
    TextRange common = dtr.intersection(element.getTextRange());
    if (common == null) {
      return null;
    }
    int startOffset = element.getTextRange().getStartOffset();
    TextRange textRange = new TextRange(common.getStartOffset() - startOffset, common.getEndOffset() - startOffset);
    return new PsiElementFragment<>(element, textRange);
  }

  public static <T extends PsiElement> PsiElementFragment<T> createWholeElement(@NotNull T element) {
    return new PsiElementFragment<>(element, element.getTextRange());
  }

  public static <T extends PsiElement> PsiElementFragment<T> create(@NotNull T element, @NotNull TextRange textRangeInElement) {
    return new PsiElementFragment<>(element, textRangeInElement);
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
