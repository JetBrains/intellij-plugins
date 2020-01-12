package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public abstract class BasicTapestryAttributeDescriptor extends BasicXmlAttributeDescriptor {

  @Override
  public void init(PsiElement element) {
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return false;
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  public boolean isEnumerated() {
    return false;
  }

  @Override
  public String @Nullable [] getEnumeratedValues() {
    return null;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }
}

