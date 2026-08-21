package org.intellij.plugins.postcss;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class PostCssBundle {

  public static @Nls String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getMessage(key, params);
  }

  private static final String PATH_TO_BUNDLE = "messages.PostCssBundle";
  private static final DynamicBundle ourInstance = new DynamicBundle(PostCssBundle.class, PATH_TO_BUNDLE);
}
