package com.jetbrains.plugins.meteor.spacebars;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;


public class MeteorTemplateNameAttributeDescriptor extends BasicXmlAttributeDescriptor {
  @Override
  public boolean isRequired() {
    return true;
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
  public PsiElement getDeclaration() {
    return null;
  }

  @Override
  public String getName() {
    return MeteorTemplateIndex.NAME_ATTRIBUTE;
  }

  @Override
  public void init(PsiElement element) {

  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String[] getEnumeratedValues() {
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }
}
