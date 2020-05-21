package org.intellij.plugins.postcss.index;

import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.psi.css.index.CssSupportedFileTypesProvider;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssSupportedFileTypesProvider extends CssSupportedFileTypesProvider {

  @NotNull
  @Override
  public LanguageFileType getSupportedFileType() {
    return PostCssFileType.POST_CSS;
  }

  @NotNull
  @Override
  public Language getLanguage() {
    return PostCssLanguage.INSTANCE;
  }

  @Nullable
  @Override
  public Lexer getIndexingLexer() {
    return null;
  }

  @NotNull
  @Override
  public Class<? extends CssCodeStyleSettings> getCustomCodeStyleSettingsClass() {
    return CssCodeStyleSettings.class;
  }
}