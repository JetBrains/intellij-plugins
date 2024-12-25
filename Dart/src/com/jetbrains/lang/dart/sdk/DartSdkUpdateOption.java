// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum DartSdkUpdateOption {
  DoNotCheck(() -> ""),
  Stable(DartBundle.messagePointer("settings.page.check.updates.combobox.item.stable.channel")),
  StableAndDev(DartBundle.messagePointer("settings.page.check.updates.combobox.item.stable.and.dev.channels"));

  public static final DartSdkUpdateOption[] OPTIONS_TO_SHOW_IN_COMBO = {Stable, StableAndDev};

  private static final String DART_CHECK_SDK_UPDATE_KEY = "DART_CHECK_SDK_UPDATE_KEY";
  private static final DartSdkUpdateOption DART_CHECK_SDK_UPDATE_DEFAULT = Stable;

  private final @NotNull Supplier<@NlsContexts.Label String> myPresentableNameSupplier;

  DartSdkUpdateOption(@NotNull Supplier<@NlsContexts.Label String> presentableNameSupplier) {
    myPresentableNameSupplier = presentableNameSupplier;
  }

  public @NotNull @NlsContexts.Label String getPresentableName() {
    return myPresentableNameSupplier.get();
  }

  public static @NotNull DartSdkUpdateOption getDartSdkUpdateOption() {
    final String value = PropertiesComponent.getInstance().getValue(DART_CHECK_SDK_UPDATE_KEY, DART_CHECK_SDK_UPDATE_DEFAULT.name());
    try {
      return valueOf(value);
    }
    catch (IllegalArgumentException e) {
      return DART_CHECK_SDK_UPDATE_DEFAULT;
    }
  }

  public static void setDartSdkUpdateOption(final @NotNull DartSdkUpdateOption option) {
    PropertiesComponent.getInstance().setValue(DART_CHECK_SDK_UPDATE_KEY, option.name(), DART_CHECK_SDK_UPDATE_DEFAULT.name());
  }
}
