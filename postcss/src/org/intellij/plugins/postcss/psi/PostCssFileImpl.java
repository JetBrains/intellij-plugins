package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.css.impl.StylesheetFileBase;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssFileImpl extends StylesheetFileBase implements PostCssFile {
  public PostCssFileImpl(FileViewProvider viewProvider) {
    super(viewProvider, PostCssLanguage.INSTANCE);
  }

  @NotNull
  public FileType getFileType() {
    return PostCssFileType.POST_CSS;
  }

  public String toString() {
    return "PostCSS File:" + getName();
  }

  @Override
  protected CssStyleSheetElementType getStylesheetElementType() {
    return PostCssElementTypes.POST_CSS_STYLESHEET;
  }
}
