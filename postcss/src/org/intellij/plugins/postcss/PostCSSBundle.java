package org.intellij.plugins.postcss;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class PostCSSBundle extends AbstractBundle {

  public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
    return ourInstance.getMessage(key, params);
  }

  private static final String PATH_TO_BUNDLE = "org.intellij.plugins.postcss.PostCSSBundle";
  private static final AbstractBundle ourInstance = new PostCSSBundle();

  private PostCSSBundle() {
    super(PATH_TO_BUNDLE);
  }
}
