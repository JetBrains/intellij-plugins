// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class Angular2Bundle extends DynamicBundle {
  @NonNls public static final String BUNDLE = "messages.Angular2Bundle";
  private static final Angular2Bundle INSTANCE = new Angular2Bundle();

  private Angular2Bundle() { super(BUNDLE); }

  public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static @NotNull Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                                         Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}