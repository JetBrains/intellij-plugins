package com.jetbrains.plugins.meteor;


import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class MeteorBundle extends DynamicBundle {

  public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getMessage(key, params);
  }

  public static final @NonNls String BUNDLE = "messages.MeteorBundle";
  private static final MeteorBundle ourInstance = new MeteorBundle();

  private MeteorBundle() {
    super(BUNDLE);
  }
}
