package com.intellij.tapestry.lang;

import com.intellij.lang.xml.XMLLanguage;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 8:10:30 PM
 */
public class TmlLanguage extends XMLLanguage {
  public static final TmlLanguage INSTANCE = new TmlLanguage();

  private TmlLanguage() {
    super(XMLLanguage.INSTANCE, "TML");
  }
}
