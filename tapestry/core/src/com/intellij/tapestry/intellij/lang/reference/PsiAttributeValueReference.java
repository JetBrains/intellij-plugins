package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.Nullable;

public class PsiAttributeValueReference extends PsiReferenceBase<XmlAttributeValue> {
  private final PsiElement _bindElement;

  public PsiAttributeValueReference(XmlAttributeValue attributeValue, PsiElement bindElement) {
    super(attributeValue);
    _bindElement = bindElement;
  }

  @Override
  @Nullable
  public PsiElement resolve() {
    return _bindElement;
  }
}
