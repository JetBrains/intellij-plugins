package org.intellij.plugins.postcss;

import com.intellij.css.util.CssConstants;
import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CssLanguageProperties;
import org.jetbrains.annotations.NotNull;

public class PostCssLanguage extends Language implements CssLanguageProperties {
  public static final PostCssLanguage INSTANCE = new PostCssLanguage();

  private PostCssLanguage(){
    super(CSSLanguage.INSTANCE, "PostCSS", "text/css");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "PostCSS";
  }

  @NotNull
  @Override
  public String getDeclarationsTerminator() {
    return CssConstants.SEMICOLON;
  }

  @Override
  public boolean isIndentBased() {
    return false;
  }

}
