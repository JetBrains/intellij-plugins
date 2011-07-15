package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.Nullable;

class XmlAttributeValueProvider implements XmlElementValueProvider {
  private XmlAttribute attribute;

  public void setAttribute(@Nullable XmlAttribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public String getTrimmed() {
    return attribute.getDisplayValue();
  }

  @Override
  public CharSequence getSubstituted() {
    return attribute.getDisplayValue();
  }

  @Override
  public XmlElement getInjectedHost() {
    return attribute.getValueElement();
  }

  @Override
  @Nullable
  public JSClass getJsClass() {
    //noinspection ConstantConditions
    return InjectionUtil.getJsClassFromPackageAndLocalClassNameReferences(attribute.getValueElement().getReferences());
  }
}
