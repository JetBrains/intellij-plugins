package com.dmarcotte.handlebars.psi;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.impl.PsiFileEx;
import org.jetbrains.annotations.NotNull;

public class HbPsiFile extends PsiFileBase implements PsiFileEx {

  public HbPsiFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, HbLanguage.INSTANCE);
  }

  @NotNull
  public FileType getFileType() {
    return HbFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "HbFile:" + getName();
  }
}
