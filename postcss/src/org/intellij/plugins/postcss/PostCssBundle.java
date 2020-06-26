package org.intellij.plugins.postcss;

import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class PostCssBundle extends DynamicBundle {

  public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getMessage(key, params);
  }

  private static final String PATH_TO_BUNDLE = "messages.PostCssBundle";
  private static final AbstractBundle ourInstance = new PostCssBundle();

  private PostCssBundle() {
    super(PATH_TO_BUNDLE);
  }
}
