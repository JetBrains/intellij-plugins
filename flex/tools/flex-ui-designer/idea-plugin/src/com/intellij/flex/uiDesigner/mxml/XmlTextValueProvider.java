package com.intellij.flex.uiDesigner.mxml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

class XmlTextValueProvider implements XmlElementValueProvider {
  private XmlText xmlText;

  public void setXmlText(XmlText xmlText) {
    this.xmlText = xmlText;
  }

  @Override
  public String getTrimmed() {
    return xmlText.getText();
  }

  @Override
  public CharSequence getSubstituted() {
    return xmlText.getValue();
  }

  @Override
  public XmlElement getInjectedHost() {
    // only tag or attribute may be queried
    throw new UnsupportedOperationException();
  }

  @Override
  public JSClass getJsClass() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public XmlElement getElement() {
    return xmlText;
  }
}
