// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.psi;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class HbPsiFile extends PsiFileBase {

  public HbPsiFile(@NotNull FileViewProvider viewProvider) {
    this(viewProvider, HbLanguage.INSTANCE);
  }

  public HbPsiFile(@NotNull FileViewProvider viewProvider, Language lang) {
    super(viewProvider, lang);
  }

  @Override
  @NotNull
  public FileType getFileType() {
    return HbFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "HbFile:" + getName();
  }
}
