package org.intellij.plugins.markdown.lang.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.jetbrains.annotations.NotNull;

public class MarkdownFile extends PsiFileBase implements MarkdownPsiElement {
  public MarkdownFile(FileViewProvider viewProvider) {
    super(viewProvider, MarkdownLanguage.INSTANCE);
  }

  @NotNull
  public FileType getFileType() {
    return MarkdownFileType.INSTANCE;
  }

}
