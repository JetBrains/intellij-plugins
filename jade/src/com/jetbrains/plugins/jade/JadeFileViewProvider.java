// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public class JadeFileViewProvider extends SingleRootFileViewProvider {

  private static final Set<Language> LANGUAGES = Collections.singleton(JadeLanguage.INSTANCE);

  public JadeFileViewProvider(final @NotNull PsiManager manager,
                              final @NotNull VirtualFile virtualFile,
                              final boolean physical) {
    super(manager, virtualFile, physical);
  }

  @Override
  public boolean supportsIncrementalReparse(final @NotNull Language rootLanguage) {
    return false;
  }

  @Override
  public @NotNull Language getBaseLanguage() {
    return JadeLanguage.INSTANCE;
  }

  @Override
  public @NotNull Set<Language> getLanguages() {
    return LANGUAGES;
  }

  @Override
  protected @Nullable PsiFile createFile(final @NotNull Language lang) {
    if (lang == getBaseLanguage()) {
      return LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);
    }
    return null;

  }
}
