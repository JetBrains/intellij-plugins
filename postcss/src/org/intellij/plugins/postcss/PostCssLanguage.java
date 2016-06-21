package org.intellij.plugins.postcss;

import com.intellij.lang.css.CSSLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssLanguage extends CSSLanguage {
  public static final PostCssLanguage INSTANCE = new PostCssLanguage();

  private PostCssLanguage(){
    super(CSSLanguage.INSTANCE, "POST_CSS", "text/css");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "PostCSS";
  }

}
