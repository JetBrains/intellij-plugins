package com.jetbrains.lang.dart.flutter;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlutterUtil {

  private static final String FLUTTER_MODULE_TYPE_ID = "FLUTTER_MODULE_TYPE";
  private static PluginId FLUTTER_PLUGIN_ID = PluginId.getId("io.flutter");

  /**
   * Get the Flutter root relative to the given Dart SDK.
   *
   * @param dartSdk the Dart SDK
   * @return the relative Flutter root, or null if this SDK is not relative to one
   */
  @Nullable
  public static String getFlutterRoot(@NotNull final String dartSdkPath) {
    final String suffix = "/bin/cache/dart-sdk";
    return dartSdkPath.endsWith(suffix) ? dartSdkPath.substring(0, dartSdkPath.length() - suffix.length()) : null;
  }

  /**
   * Test if the given project defines a Flutter module.
   *
   * @param module the project to test
   * @return true if the given project has a Flutter module, false otherwise.
   */
  public static boolean hasFlutterModule(@NotNull project) {
    return Arrays.stream(ModuleManager.getInstance(project).getModules()).anyMatch(FlutterUtil::isFlutterModule);
  }

  /**
   * Test if the given module is a Flutter module.
   *
   * @param module the module to test
   * @return true if the module is a Flutter module, false otherwise.
   */
  public static boolean isFlutterModule(@NotNull Module module) {
    final ModuleType moduleType = ModuleType.get(module);
    return FLUTTER_MODULE_TYPE_ID.equals(moduleType.getId());
  }

  /**
   * Test if the Flutter plugin is installed.
   *
   * @return true if the the Flutter plugin is installed, false otherwise.
   */
  public static boolean isFlutterPluginInstalled() {
    return PluginManager.isPluginInstalled(FLUTTER_PLUGIN_ID);
  }
}
