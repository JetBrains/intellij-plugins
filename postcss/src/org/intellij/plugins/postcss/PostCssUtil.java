package org.intellij.plugins.postcss;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PostCssUtil {

  public static GlobalSearchScope getCustomSelectorSearchScope(@NotNull final PsiElement position, @NotNull final PsiFile file) {
    if (file instanceof StylesheetFile) {
      Set<VirtualFile> importedFiles = CssUtil.getImportedFiles(file, position, true);
      return GlobalSearchScope.filesWithoutLibrariesScope(position.getProject(), importedFiles);
    }
    else {
      return GlobalSearchScope.fileScope(position.getProject(), file.getVirtualFile());
    }
  }
}