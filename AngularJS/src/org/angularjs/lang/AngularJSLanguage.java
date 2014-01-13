package org.angularjs.lang;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLanguage extends Language {
  public static AngularJSLanguage INSTANCE = new AngularJSLanguage();

  protected AngularJSLanguage() {
    super(JavascriptLanguage.INSTANCE, "AngularJS");
  }
}
