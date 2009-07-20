package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jul 20, 2009
 *         Time: 5:26:28 PM
 */
public abstract class BasicTapestryAttributeDescriptor extends BasicXmlAttributeDescriptor {

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

