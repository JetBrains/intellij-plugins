package com.jetbrains.lang.dart.flutter;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FlutterUtil {

  private static final String FLUTTER_MODULE_TYPE_NAME = "Flutter";

  /**
   * Test if the given module is a Flutter module.
   *
   * @param module the module to test
   * @return true if the module is a Flutter module, false otherwise
   */
  public static boolean isFlutterModule(@NotNull Module module) {
    final ModuleType moduleType = ModuleType.get(module);
    return FLUTTER_MODULE_TYPE_NAME.equals(moduleType.getName());
  }

  /**
   * Get the Flutter root relative to the given Dart SDK.
   *
   * @param dartSdk the Dart SDK
   * @return the relative Flutter root, or null if this SDK is not relative to one
   */
  public static String getFlutterRoot(@NotNull DartSdk dartSdk) {
    // Navigate up from `bin/cache/dart-sdk/lib/`.
    final File flutterRoot = new File(dartSdk.getHomePath() + "/../../..");
    if (flutterRoot.exists()) {
      try {
        final URI uri = new URI(flutterRoot.getAbsolutePath()).normalize();
        return uri.getPath();
      }
      catch (URISyntaxException e) {
        // Ignored
      }
    }
    return null;
  }
}
