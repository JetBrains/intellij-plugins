package com.intellij.lang.javascript.linter.eslint.config;

import com.intellij.ide.IconProvider;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class EslintConfigIconProvider extends IconProvider implements DumbAware {
  @Override
  public @Nullable Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
    if (!(element instanceof PsiFile)) {
      return null;
    }
    VirtualFile virtualFile = ((PsiFile)element).getViewProvider().getVirtualFile();
    if (EslintUtil.isFlatOrLegacyConfigFile(virtualFile) ||
        StringUtil.equals(virtualFile.getNameSequence(), EslintUtil.DEFAULT_IGNORE_FILENAME)) {
      return JavaScriptLanguageIcons.FileTypes.Eslint;
    }
    return null;
  }
}
