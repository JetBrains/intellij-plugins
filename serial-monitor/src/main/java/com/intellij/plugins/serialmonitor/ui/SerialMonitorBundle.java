package com.intellij.plugins.serialmonitor.ui;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * @author Dmitry_Cherkas
 */
public class SerialMonitorBundle {
  public static final String BUNDLE = "messages.SerialMonitorBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(SerialMonitorBundle.class, BUNDLE);

  private SerialMonitorBundle() {}

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                                              Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
