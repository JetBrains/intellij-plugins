// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.files;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlFileViewProviderFactory implements FileViewProviderFactory {
  @Override
  public @NotNull FileViewProvider createFileViewProvider(@NotNull VirtualFile file,
                                                          Language language,
                                                          @NotNull PsiManager manager,
                                                          boolean eventSystemEnabled) {
    return new CfmlFileViewProvider(manager, file, eventSystemEnabled);
  }
}
