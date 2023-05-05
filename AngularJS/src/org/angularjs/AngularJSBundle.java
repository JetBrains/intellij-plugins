// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.ibm.icu.text.MessageFormat;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class AngularJSBundle {
  public static final @NonNls String BUNDLE = "messages.AngularJSBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(AngularJSBundle.class, BUNDLE);

  private AngularJSBundle() {}

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static @NotNull @Nls String icuMessage(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return MessageFormat.format(INSTANCE.getResourceBundle().getString(key), params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                                         Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
