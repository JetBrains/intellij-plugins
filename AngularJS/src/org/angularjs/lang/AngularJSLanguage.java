package org.angularjs.lang;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLanguage extends JSLanguageDialect {
  public static final AngularJSLanguage INSTANCE = new AngularJSLanguage();

  protected AngularJSLanguage() {
    super("AngularJS", DialectOptionHolder.OTHER);
  }
}
