package org.intellij.plugins.postcss.psi.impl;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.css.impl.StylesheetFileBase;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssFile;
import org.jetbrains.annotations.NotNull;

public class PostCssFileImpl extends StylesheetFileBase implements PostCssFile {
  public PostCssFileImpl(FileViewProvider viewProvider) {
    super(viewProvider, PostCssLanguage.INSTANCE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return PostCssFileType.POST_CSS;
  }

  @Override
  public String toString() {
    return "PostCSS File:" + getName();
  }
}
