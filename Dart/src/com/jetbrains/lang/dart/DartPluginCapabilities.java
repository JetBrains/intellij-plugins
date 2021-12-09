// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * A queryable class about the capabilities of the Dart plugin.
 */
@SuppressWarnings("unused")
public final class DartPluginCapabilities {
  private static final Set<String> capabilities = new HashSet<>();

  static {
    capabilities.add("supports.pausePostRequest");
  }

  public static boolean isSupported(@NotNull String featureKey) {
    return capabilities.contains(featureKey);
  }
}
