package org.jetbrains.osgi.jps.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Consumer;
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
  @NotNull
  public static Properties getMavenProjectProperties(@NotNull CompileContext context, @NotNull JpsModule module) {
    final MavenProjectConfiguration projectConfig = getProjectConfig(context);
    final Properties result = new Properties();
    if (projectConfig == null) return result;

    JpsJavaExtensionService.dependencies(module).recursively().productionOnly().processModules(new Consumer<JpsModule>() {
      @Override
      public void consume(JpsModule module) {
        MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(module.getName());
        if (moduleConfig != null) {
          Map<String, String> properties = moduleConfig.properties;
          for (Map.Entry<String, String> entry : properties.entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
          }
        }
      }
    });
    return result;
  }

  @Nullable
  public static File getMavenProjectPath(@NotNull CompileContext context, @NotNull JpsModule module) {
    MavenProjectConfiguration projectConfig = getProjectConfig(context);
    if (projectConfig == null) return null;
    MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(module.getName());
    return moduleConfig == null ? null : new File(FileUtil.toSystemDependentName(moduleConfig.directory), "pom.xml");
  }

  @Nullable
  private static MavenProjectConfiguration getProjectConfig(CompileContext context) {
    BuildDataPaths dataPaths = context.getProjectDescriptor().dataManager.getDataPaths();
    return JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(dataPaths);
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
