package com.intellij.flex.uiDesigner.mxml;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;

class XmlAttributeValueProvider implements XmlElementValueProvider {
  private XmlAttribute attribute;

  public void setAttribute(XmlAttribute attribute) {
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
}
