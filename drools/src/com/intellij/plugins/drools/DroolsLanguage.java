// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools;

import com.intellij.lang.Language;

public class DroolsLanguage extends Language {
  public static final DroolsLanguage INSTANCE = new DroolsLanguage();

  protected DroolsLanguage() {
    super("Drools");
  }
}
