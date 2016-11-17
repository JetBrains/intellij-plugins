package com.jetbrains.lang.dart;

import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A queryable class about the capabilities of the Dart plugin.
 */
public class DartPluginCapabilities {
  public static Map<String, Boolean> capabilities = new HashMap<>();

  static {
    // TODO: set up capabilities here
  }

  public static boolean isSupported(@NotNull String featureKey) {
    return capabilities.getOrDefault(featureKey, Boolean.FALSE).booleanValue();
  }
}
