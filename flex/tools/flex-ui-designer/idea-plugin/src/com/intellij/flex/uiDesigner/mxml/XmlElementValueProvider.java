package com.intellij.flex.uiDesigner.mxml;

import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.Nullable;

interface XmlElementValueProvider {
  String EMPTY = "";
  
  String getTrimmed();

  /**
   * @see com.intellij.psi.xml.XmlText#getValue()
   * @see com.intellij.psi.xml.XmlAttribute#getDisplayValue()
   */
  CharSequence getSubstituted();
  
  @Nullable
  XmlElement getInjectedHost();
}
