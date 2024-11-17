// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.lang.Language;

public final class TslLanguage extends Language {
  public static final TslLanguage INSTANCE = new TslLanguage();

  public TslLanguage() {
    super("ToString");
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }
}
