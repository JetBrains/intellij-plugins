package com.jetbrains.lang.dart.flutter;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlutterUtil {

  private static final String FLUTTER_MODULE_TYPE_ID = "FLUTTER_MODULE_TYPE";
  private static final boolean FLUTTER_PLUGIN_INSTALLED = PluginManager.isPluginInstalled(PluginId.getId("io.flutter"));

  /**
   * @return the Flutter SDK root relative to the given Dart SDK or <code>null</code>
   */
  @Nullable
  public static String getFlutterRoot(@NotNull final String dartSdkPath) {
    final String suffix = "/bin/cache/dart-sdk";
    return dartSdkPath.endsWith(suffix) ? dartSdkPath.substring(0, dartSdkPath.length() - suffix.length()) : null;
  }

  public static boolean isFlutterModule(@NotNull final Module module) {
    return FLUTTER_MODULE_TYPE_ID.equals(ModuleType.get(module).getId());
  }

  public static boolean isFlutterPluginInstalled() {
    return FLUTTER_PLUGIN_INSTALLED;
  }
}
