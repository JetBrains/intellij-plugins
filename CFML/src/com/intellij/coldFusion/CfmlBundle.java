// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class CfmlBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "messages.CfmlBundle";
  private static final CfmlBundle INSTANCE = new CfmlBundle();

  private CfmlBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  @NotNull
  public static Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }

  // TextAttributeKey names must be globally unique.
  // However those in CfmlBundle are not. So mangle them.
  public static String cfmlizeMessage(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key) {
    return "Cfml" + message(key);
  }
}
