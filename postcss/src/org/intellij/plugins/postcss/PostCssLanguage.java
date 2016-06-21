package org.intellij.plugins.postcss;

import com.intellij.lang.css.CSSLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * PostCss language definition
 * Created by Ilya Bochkarev on 6/21/16.
 */
public class PostCssLanguage extends CSSLanguage {
  public static final PostCssLanguage INSTANCE = new PostCssLanguage();

  private PostCssLanguage(){
    super("POST_CSS", "text/css");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "PostCSS";
  }

}
