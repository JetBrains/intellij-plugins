package com.intellij.flex.uiDesigner.mxml;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;

class ValueProviderFactory {
  private final XmlTextValueProvider xmlTextValueProvider = new XmlTextValueProvider();
  private final XmlTagValueProvider xmlTagValueProvider = new XmlTagValueProvider();
  private final XmlAttributeValueProvider xmlAttributeValueProvider = new XmlAttributeValueProvider();

  XmlElementValueProvider create(XmlText xmlText) {
    xmlTextValueProvider.setXmlText(xmlText);
    return xmlTextValueProvider;
  }

  XmlTagValueProvider create(XmlTag tag) {
    xmlTagValueProvider.setTag(tag);
    return xmlTagValueProvider;
  }

  XmlElementValueProvider create(XmlAttribute attribute) {
    xmlAttributeValueProvider.setAttribute(attribute);
    return xmlAttributeValueProvider;
  }
}
