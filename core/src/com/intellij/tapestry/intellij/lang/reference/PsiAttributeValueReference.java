package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiAttributeValueReference implements PsiReference {

  private final XmlAttributeValue _attributeValue;
  private final PsiElement _bindElement;

  public PsiAttributeValueReference(XmlAttributeValue attributeValue, PsiElement bindElement) {
    _attributeValue = attributeValue;
    _bindElement = bindElement;
  }

  public PsiElement getElement() {
    return _attributeValue;
  }

  public TextRange getRangeInElement() {
    if (_attributeValue.getText().startsWith("\"") || _attributeValue.getText().startsWith("\'")) {
      return TextRange.from(1, _attributeValue.getTextRange().getLength() - 2);
    }
    return TextRange.from(0, _attributeValue.getValue().length());
  }

  @Nullable
  public PsiElement resolve() {
    return _bindElement;
  }

  public String getCanonicalText() {
    return _bindElement.getText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    throw new UnsupportedOperationException();
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    throw new UnsupportedOperationException();
  }

  public boolean isReferenceTo(PsiElement element) {
    return resolve() == element;
  }

  @NotNull
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }
}
