// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;

public final class JdlLanguage extends Language {

  public static final JdlLanguage INSTANCE = new JdlLanguage();

  private JdlLanguage() {
    super("JDL");
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public LanguageFileType getAssociatedFileType() {
    return JdlFileType.INSTANCE;
  }
}
