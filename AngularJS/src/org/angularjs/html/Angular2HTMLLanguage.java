package org.angularjs.html;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLLanguage extends XMLLanguage {
  public static final Angular2HTMLLanguage INSTANCE = new Angular2HTMLLanguage();

  protected Angular2HTMLLanguage() {
    super(HTMLLanguage.INSTANCE, "Angular2HTML");
  }
}
