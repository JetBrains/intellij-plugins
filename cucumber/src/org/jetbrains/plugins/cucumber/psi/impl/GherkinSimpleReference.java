package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;

public class GherkinSimpleReference implements PsiReference {

  private GherkinPsiElement myElement;

  public GherkinSimpleReference(GherkinPsiElement element) {
    myElement = element;
  }

  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @Override
  public TextRange getRangeInElement() {
    return new TextRange(0, myElement.getTextLength());
  }

  @Override
  public PsiElement resolve() {
    return myElement;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myElement.getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    if (myElement instanceof PsiNamedElement) {
      ((PsiNamedElement) myElement).setName(newElementName);
    }
    return myElement;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return myElement;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    PsiElement myResolved = resolve();
    PsiElement resolved = element.getReference() != null ? element.getReference().resolve() : null;
    if (resolved == null) {
      resolved = element;
    }
    return resolved != null && myResolved != null && resolved.equals(myResolved);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
