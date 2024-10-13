package org.jetbrains.qodana;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class QodanaBundle {
  private static final @NonNls String BUNDLE = "messages.QodanaBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(QodanaBundle.class, BUNDLE);

  private QodanaBundle() {}

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }
}
