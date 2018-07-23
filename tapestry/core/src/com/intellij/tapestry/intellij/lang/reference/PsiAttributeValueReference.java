package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiAttributeValueReference extends PsiReferenceBase<XmlAttributeValue> {
  private final PsiElement _bindElement;

  public PsiAttributeValueReference(XmlAttributeValue attributeValue, PsiElement bindElement) {
    super(attributeValue);
    _bindElement = bindElement;
  }

  @Nullable
  public PsiElement resolve() {
    return _bindElement;
  }

  @NotNull
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}
