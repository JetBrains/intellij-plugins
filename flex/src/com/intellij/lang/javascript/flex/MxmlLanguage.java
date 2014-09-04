package com.intellij.lang.javascript.flex;

import com.intellij.lang.xml.XMLLanguage;

public class MxmlLanguage extends XMLLanguage {
  public static final MxmlLanguage INSTANCE = new MxmlLanguage();
  
  private MxmlLanguage() {
    super(XMLLanguage.INSTANCE, "Mxml");
  }
}
