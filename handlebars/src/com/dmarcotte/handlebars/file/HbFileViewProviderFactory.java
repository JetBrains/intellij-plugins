package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;


public class HbFileViewProviderFactory implements FileViewProviderFactory {
  @NotNull
  @Override
  public FileViewProvider createFileViewProvider(@NotNull VirtualFile virtualFile,
                                                 Language language,
                                                 @NotNull PsiManager psiManager,
                                                 boolean eventSystemEnabled) {
    return new HbFileViewProvider(psiManager, virtualFile, eventSystemEnabled, HbLanguage.INSTANCE);
  }
}

