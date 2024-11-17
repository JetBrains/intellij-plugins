// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import com.intellij.tsr.TslFileType;
import com.intellij.tsr.TslLanguage;

public final class TslFile extends PsiFileBase {
  public TslFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, TslLanguage.INSTANCE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return TslFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "TslFile";
  }
}
