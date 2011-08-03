package com.intellij.flex.uiDesigner.mxml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;
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

  @Nullable
  JSClass getJsClass();

  @NotNull
  XmlElement getElement();
}
