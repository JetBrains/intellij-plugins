package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public enum DartSdkUpdateOption {
  DoNotCheck("-- doesn't matter --"),
  Stable("Stable channel"),
  // Dev("Dev channel"),
  StableAndDev("Stable and Dev channels");

  public static final DartSdkUpdateOption[] OPTIONS_TO_SHOW_IN_COMBO = {Stable, StableAndDev};

  private static final String DART_CHECK_SDK_UPDATE_KEY = "DART_CHECK_SDK_UPDATE_KEY";
  private static final DartSdkUpdateOption DART_CHECK_SDK_UPDATE_DEFAULT = Stable;

  @NotNull private final String myPresentableName;

  DartSdkUpdateOption(@NotNull final String presentableName) {
    myPresentableName = presentableName;
  }

  @NotNull
  public String getPresentableName() {
    return myPresentableName;
  }

  @NotNull
  public static DartSdkUpdateOption getDartSdkUpdateOption() {
    final String value = PropertiesComponent.getInstance().getValue(DART_CHECK_SDK_UPDATE_KEY, DART_CHECK_SDK_UPDATE_DEFAULT.name());
    try {
      return valueOf(value);
    }
    catch (IllegalArgumentException e) {
      return DART_CHECK_SDK_UPDATE_DEFAULT;
    }
  }

  public static void setDartSdkUpdateOption(@NotNull final DartSdkUpdateOption option) {
    PropertiesComponent.getInstance().setValue(DART_CHECK_SDK_UPDATE_KEY, option.name(), DART_CHECK_SDK_UPDATE_DEFAULT.name());
  }
}
