package org.intellij.plugins.markdown;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

public class MarkdownBundle {
  @NotNull
  private static final String BUNDLE_NAME = "org.intellij.plugins.markdown.bundle.MarkdownBundle";
  @NotNull
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
    return CommonBundle.message(BUNDLE, key, params);
  }
}
