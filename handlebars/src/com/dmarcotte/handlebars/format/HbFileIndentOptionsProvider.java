package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.lang.Language;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.FileIndentOptionsProvider;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbFileIndentOptionsProvider extends FileIndentOptionsProvider {
  @Nullable
  @Override
  public CommonCodeStyleSettings.IndentOptions getIndentOptions(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
    if (file instanceof HbPsiFile) {
      FileViewProvider provider = file.getViewProvider();
      if (provider instanceof TemplateLanguageFileViewProvider) {
        Language language = ((TemplateLanguageFileViewProvider)provider).getTemplateDataLanguage();
        return settings.getCommonSettings(language).getIndentOptions();
      }
    }
    return null;
  }
}
