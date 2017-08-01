package com.jetbrains.lang.dart.flutter;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlutterUtil {

  private static final String FLUTTER_MODULE_TYPE_ID = "FLUTTER_MODULE_TYPE";
  private static final String DART_SDK_SUFFIX = "/bin/cache/dart-sdk";
  private static final boolean FLUTTER_PLUGIN_INSTALLED = PluginManager.isPluginInstalled(PluginId.getId("io.flutter"));

  /**
   * @return the Flutter SDK root relative to the given Dart SDK or {@code null}
   */
  @Nullable
  public static String getFlutterRoot(@NotNull final String dartSdkPath) {
    return dartSdkPath.endsWith(DART_SDK_SUFFIX) ? dartSdkPath.substring(0, dartSdkPath.length() - DART_SDK_SUFFIX.length()) : null;
  }

  public static boolean isFlutterModule(@NotNull final Module module) {
    // Check the module type for backwards compatibility:
    if (FLUTTER_MODULE_TYPE_ID.equals(ModuleType.get(module).getId())) return true;

    // Flutter module support is now defined by:
    // [Flutter support enabled for a module] ===
    // [Dart support enabled && referenced Dart SDK is the one inside a Flutter SDK]
    final DartSdk dartSdk = DartSdk.getDartSdk(module.getProject());
    final String dartSdkPath = dartSdk != null ? dartSdk.getHomePath() : null;
    return dartSdkPath != null && dartSdkPath.endsWith(DART_SDK_SUFFIX) && DartSdkLibUtil.isDartSdkEnabled(module);
  }

  public static boolean isFlutterPluginInstalled() {
    return FLUTTER_PLUGIN_INSTALLED;
  }
}
