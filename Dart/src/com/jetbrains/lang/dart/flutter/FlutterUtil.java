// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.flutter;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class FlutterUtil {
  private static final String FLUTTER_MODULE_TYPE_ID = "FLUTTER_MODULE_TYPE";
  private static final String DART_SDK_SUFFIX = "/bin/cache/dart-sdk";
  private static final boolean FLUTTER_PLUGIN_INSTALLED = PluginManagerCore.isPluginInstalled(PluginId.getId("io.flutter"));

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

  /**
   * Returns true if the passed pubspec declares a flutter dependency.
   * <p/>
   * This method intentionally matches the contents in PubRoot.declaresFlutter in the Flutter plugin, which ensures that
   * the same logic is used between the two plugins to avoid issues like this:
   * https://github.com/flutter/flutter-intellij/issues/1445
   */
  public static boolean isPubspecDeclaringFlutter(@NotNull final VirtualFile pubspec) {
    // It uses Flutter if it contains:
    // dependencies:
    //   flutter:

    try {
      final String contents = new String(pubspec.contentsToByteArray(true /* cache contents */), StandardCharsets.UTF_8);
      final Map<String, Object> yaml = loadPubspecInfo(contents);
      if (yaml == null) {
        return false;
      }

      final Object flutterEntry = yaml.get("dependencies");
      //noinspection SimplifiableIfStatement
      if (flutterEntry instanceof Map) {
        return ((Map<?, ?>)flutterEntry).containsKey("flutter");
      }

      return false;
    }
    catch (IOException e) {
      return false;
    }
  }

  /**
   * See comment above in {@link #isPubspecDeclaringFlutter(VirtualFile)}. This method was also copied from PubRoot.
   */
  private static Map<String, Object> loadPubspecInfo(@NotNull String yamlContents) {
    final Yaml yaml = new Yaml(new SafeConstructor(), new Representer(), new DumperOptions(), new Resolver() {
      @Override
      protected void addImplicitResolvers() {
        this.addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
        this.addImplicitResolver(Tag.NULL, NULL, "~nN\u0000");
        this.addImplicitResolver(Tag.NULL, EMPTY, null);
        this.addImplicitResolver(new Tag("tag:yaml.org,2002:value"), VALUE, "=");
        this.addImplicitResolver(Tag.MERGE, MERGE, "<");
      }
    });

    try {
      //noinspection unchecked
      return yaml.load(yamlContents);
    }
    catch (Exception e) {
      return null;
    }
  }
}
