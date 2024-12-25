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
  private static final @NonNls String BUNDLE = "messages.PerforceBundle";
  public static final PerforceBundle INSTANCE = new PerforceBundle();

  private PerforceBundle() { super(BUNDLE); }

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.containsKey(key) ? INSTANCE.getMessage(key, params) : PerforceDeprecatedMessagesBundle.message(key, params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.containsKey(key) ? INSTANCE.getLazyMessage(key, params) : PerforceDeprecatedMessagesBundle.messagePointer(key, params);
  }
}
