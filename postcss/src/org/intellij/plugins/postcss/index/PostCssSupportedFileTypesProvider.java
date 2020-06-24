package org.intellij.plugins.postcss.index;

import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.index.CssSupportedFileTypesProvider;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssHighlightingLexer;
import org.jetbrains.annotations.NotNull;

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

  @NotNull
  @Override
  public Lexer getIndexingLexer() {
    return new PostCssHighlightingLexer(CssElementDescriptorFactory2.getInstance().getValueIdentifiers());
  }

  @NotNull
  @Override
  public Class<? extends CssCodeStyleSettings> getCustomCodeStyleSettingsClass() {
    return CssCodeStyleSettings.class;
  }
}