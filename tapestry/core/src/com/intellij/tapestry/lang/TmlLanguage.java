package com.intellij.tapestry.lang;

import com.intellij.lang.xml.XMLLanguage;

/**
 * @author Alexey Chmutov
 */
public final class TmlLanguage extends XMLLanguage {
  public static final TmlLanguage INSTANCE = new TmlLanguage();

  private TmlLanguage() {
    super(XMLLanguage.INSTANCE, "TML");
  }
}
