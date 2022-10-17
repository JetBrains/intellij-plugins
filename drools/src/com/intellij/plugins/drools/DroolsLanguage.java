package com.intellij.plugins.drools;

import com.intellij.lang.Language;

public class DroolsLanguage extends Language {
  public static final DroolsLanguage INSTANCE = new DroolsLanguage();

  protected DroolsLanguage() {
    super("Drools");
  }
}
