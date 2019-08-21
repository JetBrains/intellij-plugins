package com.jetbrains.lang.dart;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.Language;

public class DartInHtmlLanguage extends Language implements DependentLanguage {
  public static DartInHtmlLanguage INSTANCE = new DartInHtmlLanguage();

  protected DartInHtmlLanguage() {
    super(DartLanguage.INSTANCE, "Dart in Html");
  }
}
