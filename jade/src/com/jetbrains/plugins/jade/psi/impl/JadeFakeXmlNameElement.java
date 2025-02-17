package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.Language;
import com.intellij.psi.impl.source.xml.XmlElementImpl;
import com.intellij.psi.xml.XmlTokenType;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;

public class JadeFakeXmlNameElement extends XmlElementImpl {
  public JadeFakeXmlNameElement() {
    super(XmlTokenType.XML_NAME);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
