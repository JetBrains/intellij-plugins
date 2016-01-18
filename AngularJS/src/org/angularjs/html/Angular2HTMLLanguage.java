package org.angularjs.html;

import com.intellij.lang.xml.XMLLanguage;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLLanguage extends XMLLanguage {
  public static final Angular2HTMLLanguage INSTANCE = new Angular2HTMLLanguage();

  protected Angular2HTMLLanguage() {
    super(XMLLanguage.INSTANCE, "Angular2HTML");
  }
}
