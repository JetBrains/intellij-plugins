package com.jetbrains.lang.dart.flutter;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlutterUtil {

  private static final String FLUTTER_MODULE_TYPE_ID = "FLUTTER_MODULE_TYPE";

  /**
   * Test if the given module is a Flutter module.
   *
   * @param module the module to test
   * @return true if the module is a Flutter module, false otherwise
   */
  public static boolean isFlutterModule(@NotNull Module module) {
    final ModuleType moduleType = ModuleType.get(module);
    return FLUTTER_MODULE_TYPE_ID.equals(moduleType.getId());
  }

  /**
   * Get the Flutter root relative to the given Dart SDK.
   *
   * @param dartSdk the Dart SDK
   * @return the relative Flutter root, or null if this SDK is not relative to one
   */
  @Nullable
  public static String getFlutterRoot(@NotNull DartSdk dartSdk) {
    final String dartPath = dartSdk.getHomePath();
    final String suffix = "/bin/cache/dart-sdk";
    return dartPath.endsWith(suffix) ? dartPath.substring(0, dartPath.length() - suffix.length()) : null;
  }
}
