package com.dmarcotte.handlebars.file;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;


public class HbFileViewProviderFactory implements FileViewProviderFactory {
  @Override
  public FileViewProvider createFileViewProvider(@NotNull VirtualFile virtualFile,
                                                 Language language,
                                                 @NotNull PsiManager psiManager,
                                                 boolean physical) {
    return new HbFileViewProvider(psiManager, virtualFile, physical);
  }
}

