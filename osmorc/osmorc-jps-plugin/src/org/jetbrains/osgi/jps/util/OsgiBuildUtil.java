// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.util;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.maven.model.JpsMavenExtensionService;
import org.jetbrains.jps.maven.model.impl.MavenModuleResourceConfiguration;
import org.jetbrains.jps.maven.model.impl.MavenProjectConfiguration;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public final class OsgiBuildUtil {
  private static final boolean ourMavenPluginLoaded;

  static {
    boolean pluginLoaded = false;
    try {
      Class.forName("org.jetbrains.jps.maven.model.JpsMavenExtensionService");
      pluginLoaded = true;
    }
    catch (ClassNotFoundException ignored) { }
    ourMavenPluginLoaded = pluginLoaded;
  }

  @NotNull
  public static Properties getMavenProjectProperties(@NotNull CompileContext context, @NotNull JpsModule module) {
    Properties result = new Properties();
    if (ourMavenPluginLoaded) collectMavenProjectProperties(context, module, result);
    return result;
  }

  @Nullable
  public static File getMavenProjectPath(@NotNull CompileContext context, @NotNull JpsModule module) {
    return ourMavenPluginLoaded ? findMavenProjectPath(context, module) : null;
  }

  private static void collectMavenProjectProperties(CompileContext context, JpsModule module, final Properties result) {
    BuildDataPaths dataPaths = context.getProjectDescriptor().dataManager.getDataPaths();
    MavenProjectConfiguration projectConfig = JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(dataPaths);
    if (projectConfig != null) {
      JpsJavaExtensionService.dependencies(module).recursively().productionOnly().processModules(module1 -> {
        MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(module1.getName());
        if (moduleConfig != null) {
          for (Map.Entry<String, String> entry : moduleConfig.properties.entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
          }
        }
      });
    }
  }

  private static File findMavenProjectPath(CompileContext context, JpsModule module) {
    BuildDataPaths dataPaths = context.getProjectDescriptor().dataManager.getDataPaths();
    return findMavenProjectPath(dataPaths, module);
  }

  @Nullable
  public static File findMavenProjectPath(BuildDataPaths dataPaths, JpsModule module) {
    MavenProjectConfiguration projectConfig = JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(dataPaths);
    if (projectConfig != null) {
      MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(module.getName());
      if (moduleConfig != null) {
        return new File(FileUtil.toSystemDependentName(moduleConfig.directory), "pom.xml");
      }
    }
    return null;
  }

  @Nullable
  public static File findFileInModuleContentRoots(@NotNull JpsModule module, @NotNull String relativePath) {
    String ioRelativePath = FileUtil.toSystemDependentName(relativePath);
    for (String rootUrl : module.getContentRootsList().getUrls()) {
      File root = JpsPathUtil.urlToFile(rootUrl);
      File result = new File(root, ioRelativePath);
      if (result.exists()) {
        return result;
      }
    }
    return null;
  }
}
