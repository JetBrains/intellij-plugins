/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class OsgiBuildUtil {
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