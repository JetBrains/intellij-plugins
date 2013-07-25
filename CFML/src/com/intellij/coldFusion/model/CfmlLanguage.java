package com.intellij.coldFusion.model;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlLanguage extends Language implements TemplateLanguage {
  public static final CfmlLanguage INSTANCE = new CfmlLanguage();

  public static final String CF8 = "cf8_tags.xml";
  public static final String CF9 = "tags.xml";
  /* todo: add tags and functions
  *  http://help.adobe.com/en_US/ColdFusion/10.0/CFMLRef/WSe9cbe5cf462523a0dd03b2c1223a399518-8000.html
  *  http://help.adobe.com/en_US/ColdFusion/10.0/CFMLRef/WS890819DC-DE4D-4b24-A237-6E3483E9D6A1.html
  * */
  public static final String CF10 = "cf10_tags.xml";
  public static final String RAILO = "Railo_tags.xml";

  private CfmlLanguage() {
    super("CFML");
  }
}
