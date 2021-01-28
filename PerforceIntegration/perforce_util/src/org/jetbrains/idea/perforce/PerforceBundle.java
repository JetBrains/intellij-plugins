/*
 * Copyright (c) 2005 JetBrains s.r.o. All Rights Reserved.
 */
package org.jetbrains.idea.perforce;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class PerforceBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "messages.PerforceBundle";
  public static final PerforceBundle INSTANCE = new PerforceBundle();

  private PerforceBundle() { super(BUNDLE); }

  @NotNull
  public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  @NotNull
  public static Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
