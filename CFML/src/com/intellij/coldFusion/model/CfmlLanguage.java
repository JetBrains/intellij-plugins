// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlLanguage extends Language implements TemplateLanguage {
  public static final CfmlLanguage INSTANCE = new CfmlLanguage();

  public static final String CF8 = "cf8_tags.xml";
  public static final String CF9 = "tags.xml";
  public static final String CF10 = "cf10_tags.xml";
  public static final String CF11 = "cf11_tags.xml";
  public static final String RAILO = "Railo_tags.xml";
  public static final String LUCEE = "Lucee_tags.xml";

  private CfmlLanguage() {
    super("CFML");
  }
}
