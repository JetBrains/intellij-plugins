package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.FileIndentOptionsProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbFileIndentOptionsProvider extends FileIndentOptionsProvider {
  @Nullable
  @Override
  public CommonCodeStyleSettings.IndentOptions getIndentOptions(@NotNull Project project,
                                                                @NotNull CodeStyleSettings settings,
                                                                @NotNull VirtualFile file) {
    if (file.getFileType().equals(HbFileType.INSTANCE)) {
      FileViewProvider provider = PsiManagerEx.getInstanceEx(project).findViewProvider(file);
      if (provider instanceof TemplateLanguageFileViewProvider) {
        Language language = ((TemplateLanguageFileViewProvider)provider).getTemplateDataLanguage();
        return settings.getCommonSettings(language).getIndentOptions();
      }
    }
    return null;
  }
}
