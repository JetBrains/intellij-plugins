package com.intellij.flex.uiDesigner.mxml;

import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.meta.PsiMetaData;
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
    return getSubstituted(xmlText);
  }

  public static CharSequence getSubstituted(XmlText xmlText) {
    return xmlText.getValue();
  }

  @Override
  public XmlElement getInjectedHost() {
    return xmlText;
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

  @Override
  public PsiMetaData getPsiMetaData() {
    return null;
  }
}
