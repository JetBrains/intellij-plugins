package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 2:45:07 PM
 */
public class TapestryIdOrTypeAttributeDescriptor extends BasicXmlAttributeDescriptor {
  private final String myName;

  public TapestryIdOrTypeAttributeDescriptor(@NotNull String name) {
    myName = name;
  }

  public PsiElement getDeclaration() {
    return null;
  }

  public String getName() {
    return myName;
  }

  public void init(PsiElement element) {
  }

  public Object[] getDependences() {
    return new Object[0];
  }

  public boolean isRequired() {
    return false;
  }

  public boolean isFixed() {
    return false;
  }

  public boolean hasIdType() {
    return false;
  }

  public boolean hasIdRefType() {
    return false;
  }

  public boolean isEnumerated() {
    return false;
  }

  @Nullable
  public String[] getEnumeratedValues() {
    return null;
  }

  public String getDefaultValue() {
    return null;
  }
}